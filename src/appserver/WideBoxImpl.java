package appserver;

import auxiliary.*;
import appserver.expringmap.map.ConcurrentHashMapWithTimedEviction;

import javax.xml.crypto.Data;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class WideBoxImpl extends UnicastRemoteObject implements WideBoxIF, ConnectionHandler.ConnectionWatcher {

    private int EXPIRING_DURATION;
    private int NUM_THEATERS;
    private int ID;
    private int NUM_SERVERS;
    private int lastTheater;
    private int firstTheater;
    private int lastBackupTheater;
    private int firstBackupTheater;

    private DataStorageIF dataStorageStub;
    private DataStorageIF dataStorageStubPrimary;
    private  DataStorageIF dataStorageStubBackup;
    private DataStorageIF dataStorageStubAsBackup;

    private int clientCounter;

    private ConcurrentHashMap<String, ConcurrentHashMapWithTimedEviction<String, Integer>> reservedSeats;

    private LinkedHashMap theaters;

    private boolean dbServerLocalMode;

    public WideBoxImpl(ConnectionHandler connector, int total_num_of_servers, int total_num_of_theaters) throws RemoteException {

        System.out.println("Widebox starting");

        dbServerLocalMode = false;

        this.ID = connector.numServersAtStart;

        WideBoxConfigHandler configfile = new WideBoxConfigHandler();
        System.out.println(configfile.toString());

        this.NUM_SERVERS = configfile.getNum_servers();
        this.NUM_THEATERS = configfile.getNum_theaters();
        this.EXPIRING_DURATION = configfile.getExpiring_time();

        int previousID = Math.floorMod((ID - 1), NUM_SERVERS);
        int succesiveID = Math.floorMod((ID + 1), NUM_SERVERS);


        this.firstTheater = ID * NUM_THEATERS / NUM_SERVERS;
        if (ID == NUM_SERVERS -1) {
            this.lastTheater = NUM_THEATERS;
        } else {
            this.lastTheater = succesiveID * NUM_THEATERS / NUM_SERVERS;
        }

        this.firstBackupTheater = previousID * NUM_THEATERS / NUM_SERVERS;
        if (ID == 0) {
            this.lastBackupTheater = NUM_THEATERS;
        } else {
            this.lastBackupTheater = (ID) * NUM_THEATERS / NUM_SERVERS;
        }




        this.reservedSeats = new ConcurrentHashMap<String, ConcurrentHashMapWithTimedEviction<String, Integer>>(lastTheater - firstTheater);
        this.theaters = new LinkedHashMap<String, Theater>();


        if (!dbServerLocalMode) {
            try {
                dataStorageStubPrimary = (DataStorageIF) connector.get("dbserver" + ID, "/dbserver");
                connector.setWatch(this,  "/dbserver/dbserver"+ID);
                System.out.println("WIDEBOXIMPL: found primary dbserver" + ID);

                dataStorageStubBackup = (DataStorageIF) connector.get("dbserver" + succesiveID, "/dbserver");
                System.out.println("WIDEBOXIMPL: found backup dbserver" + succesiveID);

                dataStorageStubAsBackup = (DataStorageIF) connector.get("dbserver" + previousID, "/dbserver");
                System.out.println("WIDEBOXIMPL: found  dbserver" + previousID + ", needed when acting as Backup Server");
            } catch (Exception e) {
                System.err.println("WIDEBOXIMPL: exception: " + e.toString());
                e.printStackTrace();
            }

            // default case: (but can be changed if primaryDB is down or WideBox is acting as backup server
            dataStorageStub = dataStorageStubPrimary;
        } else {
            for (int i = 0; i < 1500; i++) {
                this.theaters.put("TheaterNr" + i, new Theater("TheaterNr" + i));
            }
            this.clientCounter = 0;
        }
        System.out.println("WIDEBOXIMPL: ready..");
    }



    @Override
    public String[] getNames () throws RemoteException {
        if (dbServerLocalMode) {
            java.util.Set keys = this.theaters.keySet();
            return (String[]) keys.toArray(new String[keys.size()]);

        } else {
            System.out.println("WIDEBOXIMPL: getNames from dbserver");
            return dataStorageStub.getTheaterNames();
        }
    }

    @Override
    public Message query(String theaterName) throws RemoteException {

        // check if the theater is in this servers responsibility
        int theaterNr = Integer.parseInt(theaterName.substring(9));
        boolean requestAsBackupServer = theaterNr >= firstBackupTheater && theaterNr < lastBackupTheater;
        DataStorageIF tempStub = null;
        if (requestAsBackupServer) {
            // the request is done to this server as backup server
            tempStub = dataStorageStub;
            dataStorageStub = dataStorageStubAsBackup;
        }
        else if (! (theaterNr >= firstTheater && theaterNr < lastTheater)) {
            // the request is not for this server at all
            return new Message(MessageType.NOT_RESPONSIBLE_ERROR);
        }

        //if the theater is queried the first time, the name is added to the HasMap
        //and a new ExpiringMap is created for the Seats and the Expirer is started for this seat map
        if (!reservedSeats.containsKey(theaterName)) {
            ConcurrentHashMapWithTimedEviction<String, Integer> expiringSeatMap =
                    new ConcurrentHashMapWithTimedEviction<>();
            reservedSeats.put(theaterName, expiringSeatMap);
        }

        Theater theater;
        synchronized (reservedSeats.get(theaterName)) {
            if (dbServerLocalMode) {
                theater = (Theater) this.theaters.get(theaterName);
                theater = theater.clone();
            } else {
                try {
                    theater = dataStorageStub.getTheater(theaterName);
                }catch (ConnectException e1) {
                    System.err.println("WIDEBOXIMPL ERROR RMI: Could not connect to primary DBServer.");
                    dataStorageStub = dataStorageStubBackup;
                    System.out.println("WIDEBOXIMPL : switched to backup DBServer" + ((ID +1) % NUM_SERVERS));

                    theater = dataStorageStub.getTheater(theaterName);
                }
            }

            if (theater.status == TheaterStatus.FULL) {
                return new Message(MessageType.FULL);
            }
            //add all reserved seats from one theater to the theaterObject as reserved
            for (String s : reservedSeats.get(theaterName).keySet()) {
                theater.setSeatToReserved(s);
            }
            //reserve a new Seat for the client
            Seat seat = theater.reserveSeat();

            if (seat == null) {
                //TODO: this is not entirly correct because maybe another client will delete its reservation and therefore
                //the theater will be free again
                return new Message(MessageType.FULL);
            }

            clientCounter++;
            int clientID = clientCounter;

            reservedSeats.get(theater.theaterName).put(seat.getSeatName(), clientID, EXPIRING_DURATION);

            if (requestAsBackupServer) {
                System.out.println("QUERY AS BACKUP from " + clientCounter + ": reservedSeats @ " + theater.theaterName + ": " + reservedSeats.get(theater.theaterName).keySet());
                dataStorageStub = tempStub;
            }
            System.out.println("QUERY from " + clientCounter + ": reservedSeats @ " + theater.theaterName + ": " + reservedSeats.get(theater.theaterName).keySet());

            return new Message(MessageType.AVAILABLE, theater.seats, seat, clientID);
        }
    }

    @Override
    public Message reserve(String theaterName, Seat old_seat, Seat wish_seat, int clientID) throws RemoteException {

        // check if the theater is in this servers responsibility
        int theaterNr = Integer.parseInt(theaterName.substring(9));
        boolean requestAsBackupServer = theaterNr >= firstBackupTheater && theaterNr < lastBackupTheater;
        DataStorageIF tempStub = null;
        if (requestAsBackupServer) {
            // the request is done to this server as backup server
            tempStub = dataStorageStub;
            dataStorageStub = dataStorageStubAsBackup;
        }
        else if (! (theaterNr >= firstTheater && theaterNr < lastTheater)) {
            // the request is not for this server at all
            return new Message(MessageType.NOT_RESPONSIBLE_ERROR);
        }

        // check if this AppServer has this theater in its list SHOULD NEVER HAPPEN
        if (reservedSeats.get(theaterName) == null) {
            return new Message(MessageType.RESERVE_ERROR);
        }

        Theater theater;
        synchronized (reservedSeats.get(theaterName)) {
            if (dbServerLocalMode) {
                theater = (Theater) this.theaters.get(theaterName);
                theater = theater.clone();
            } else {
                try {
                    theater = dataStorageStub.getTheater(theaterName);
                }catch (ConnectException e1) {
                    System.err.println("WIDEBOXIMPL ERROR RMI: Could not connect to primary DBServer.");
                    dataStorageStub = dataStorageStubBackup;
                    System.out.println("WIDEBOXIMPL : switched to backup DBServer" + ((ID +1) % NUM_SERVERS));

                    theater = dataStorageStub.getTheater(theaterName);
                }
            }

            //add all reserved seats from one theater to the theaterObject as reserved
            for (String s : reservedSeats.get(theaterName).keySet()) {
                theater.setSeatToReserved(s);
            }

            Seat return_seat;

            //try to reserve a new Seat for the client
            Seat new_seat = theater.reserveSeat(wish_seat);
            if (new_seat != null) {
                reservedSeats.get(theaterName).put(new_seat.getSeatName(), clientID, EXPIRING_DURATION);

                //when reserve happens before 15 sec timeout, we need to remove the old reserved seat
                if (reservedSeats.get(theaterName).get(old_seat.getSeatName()) != null ||
                        reservedSeats.get(theaterName).get(old_seat.getSeatName()) == clientID) {
                    reservedSeats.get(theaterName).remove(old_seat.getSeatName());
                }
                theater.freeSeat(old_seat);

                return_seat = new_seat;
            }
            // if the wish seat was not free, just return the old seat
            else {
                return_seat = old_seat;
            }

            if (requestAsBackupServer) {
                System.out.println("RESERVE AS BACKUP from " + clientCounter + ":  @ " + theater.theaterName + ": " + return_seat.getSeatName());
                dataStorageStub = tempStub;
            }
            System.out.println("RESERVE from " + clientCounter + ":  @ " + theater.theaterName + ": " + return_seat.getSeatName());

            return new Message(MessageType.AVAILABLE, theater.seats, return_seat, clientID);
        }
    }

    @Override
    public Message accept(String theaterName, Seat acceptedSeat, int clientID) throws RemoteException {

        // check if the theater is in this servers responsibility
        int theaterNr = Integer.parseInt(theaterName.substring(9));
        boolean requestAsBackupServer = theaterNr >= firstBackupTheater && theaterNr < lastBackupTheater;
        DataStorageIF tempStub = null;
        if (requestAsBackupServer) {
            // the request is done to this server as backup server
            tempStub = dataStorageStub;
            dataStorageStub = dataStorageStubAsBackup;
        }
        else if (! (theaterNr >= firstTheater && theaterNr < lastTheater)) {
            // the request is not for this server at all
            return new Message(MessageType.NOT_RESPONSIBLE_ERROR);
        }

        // check if this AppServer has this theater in its list
        // check if this client has already that seat reserved, if not, return error
        if (reservedSeats.get(theaterName) == null ||
                reservedSeats.get(theaterName).get(acceptedSeat.getSeatName()) == null ||
                    reservedSeats.get(theaterName).get(acceptedSeat.getSeatName()) != clientID) {
            return new Message(MessageType.ACCEPT_ERROR);
        }

        Theater theater;
        if (dbServerLocalMode) {
            theater = (Theater) this.theaters.get(theaterName);
            if (theater.occupySeat(acceptedSeat)) {
                reservedSeats.get(theaterName).remove(acceptedSeat.getSeatName());
                return new Message(MessageType.ACCEPT_OK);
            } else {
                return new Message(MessageType.ACCEPT_ERROR);
            }
        } else {
            synchronized (reservedSeats.get(theaterName)) {
            
                //try to buy the seat at the DB Server
                boolean success;
                try {
                    success = dataStorageStub.occupySeat(theaterName, acceptedSeat);
                }catch (ConnectException e1) {
                    System.err.println("WIDEBOXIMPL ERROR RMI: Could not connect to primary DBServer.");
                    dataStorageStub = dataStorageStubBackup;
                    System.out.println("WIDEBOXIMPL : switched to backup DBServer" + ((ID +1) % NUM_SERVERS));

                    success = dataStorageStub.occupySeat(theaterName, acceptedSeat);
                }


                if (success) {
                    // if something goes wrong, the reservation will be kept, otherwise remove reservation
                    reservedSeats.get(theaterName).remove(acceptedSeat.getSeatName());

                    if (requestAsBackupServer) {
                        System.out.println("ACCEPT AS BACKUP from " + clientCounter + ":  @ " + theaterName + ": " + acceptedSeat.getSeatName());
                        dataStorageStub = tempStub;
                    }
                    System.out.println("ACCEPT from " + clientCounter + ":  @ " + theaterName + ": " + acceptedSeat.getSeatName());

                    return new Message(MessageType.ACCEPT_OK);

                } else {
                    if (requestAsBackupServer) {
                        System.err.println("ACCEPT ERROR AS BACKUP from " + clientCounter + ":  @ " + theaterName + ": " + acceptedSeat.getSeatName());
                        dataStorageStub = tempStub;
                    }
                    System.err.println("ACCEPT ERROR from " + clientCounter + ":  @ " + theaterName + ": " + acceptedSeat.getSeatName());

                    return new Message(MessageType.ACCEPT_ERROR);
                }
            }
        }


    }

    @Override
    public Message cancel(String theaterName, Seat seat, int clientID) throws RemoteException {

        // check if this AppServer has this theater in its list
        // check if this client has already that seat reserved, if not, return error
        if (reservedSeats.get(theaterName) == null ||
                reservedSeats.get(theaterName).get(seat.getSeatName()) == null ||
                    reservedSeats.get(theaterName).get(seat.getSeatName()) != clientID) {
            return new Message(MessageType.CANCEL_ERROR);
        } else {
            reservedSeats.get(theaterName).remove(seat.getSeatName());
            return new Message(MessageType.CANCEL_OK);
        }
    }


    @Override
    public void killServer() throws RemoteException {
        System.exit(0);
    }

    @Override
    public void connectionLost(String path) {

        String[] path_componenets = path.split("/");
        String serverName = path_componenets[path_componenets.length - 1];
        System.out.println("WIDEBOXIMPL WATCH: connection lost to " + serverName);

        dataStorageStub = dataStorageStubBackup;
        System.out.println("WIDEBOXIMPL WATCH: switched to backup dbserver" + ((ID +1) % NUM_SERVERS));
    }

}
