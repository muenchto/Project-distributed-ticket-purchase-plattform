package server;


import auxiliary.*;
import expringMap.map.ConcurrentHashMapWithTimedEviction;

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

    private static final long EXPIRING_DURATION = 15000;
    private int ID;
    private int NUM_SERVERS;

    private DataStorageIF dataStorageStub;
    private  DataStorageIF dataStorageStubBackup;

    private int clientCounter;

    private ConcurrentHashMap<String, ConcurrentHashMapWithTimedEviction<String, Integer>> reservedSeats;

    private LinkedHashMap theaters;

    private boolean dbServerLocalMode;

    public WideBoxImpl(ConnectionHandler connector, int NUM_SERVERS) throws RemoteException {

        dbServerLocalMode = false;

        this.ID = connector.numServersAtStart;
        this.NUM_SERVERS = NUM_SERVERS;

        System.out.println("Widebox starting");

        this.reservedSeats = new ConcurrentHashMap<String, ConcurrentHashMapWithTimedEviction<String, Integer>>(1500);
        this.theaters = new LinkedHashMap<String, Theater>();


        if (!dbServerLocalMode) {
            try {
                dataStorageStub = (DataStorageIF) connector.get("dbserver" + ID, "/dbserver");
                connector.setWatch(this,  "/dbserver/dbserver"+ID);
                System.out.println("WIDEBOXIMPL: found primary dbserver" + ID);
                dataStorageStubBackup = (DataStorageIF) connector.get("dbserver" + ((ID +1) % NUM_SERVERS), "/dbserver");
                System.out.println("WIDEBOXIMPL: found backup dbserver" + ((ID +1) % NUM_SERVERS));
            } catch (Exception e) {
                System.err.println("WIDEBOXIMPL: exception: " + e.toString());
                e.printStackTrace();
            }
        } else {
            for (int i = 0; i < 1500; i++) {
                this.theaters.put("TheaterNr" + i, new Theater("TheaterNr" + i));
            }
            this.clientCounter = 0;
        }
        System.out.println("WIDEBOXIMPL: ready..");

        }


        public String[] getNames () throws RemoteException {
            if (dbServerLocalMode) {
                java.util.Set keys = this.theaters.keySet();
                String[] names = (String[]) keys.toArray(new String[keys.size()]);
                return names;

            } else {
                System.out.println("getNames");
                String[] temp = dataStorageStub.getTheaterNames();
                return temp;
            }
        }

    @Override
    public Message query(String theaterName) throws RemoteException {

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


            System.out.println("QUERY from " + clientCounter + ": reservedSeats @ " + theater.theaterName + ": " + reservedSeats.get(theater.theaterName).keySet());

            return new Message(MessageType.AVAILABLE, theater.seats, seat, clientID);
        }
    }

    @Override
    public Message reserve(String theaterName, Seat old_seat, Seat wish_seat, int clientID) throws RemoteException {

        // check if this AppServer has this theater in its list
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

                return new Message(MessageType.AVAILABLE, theater.seats, new_seat, clientID);
            }

            // if the wish seat was not free, just return the old seat
            else {
                return new Message(MessageType.AVAILABLE, theater.seats, old_seat, clientID);
            }
        }
    }

    @Override
    public Message accept(String theaterName, Seat acceptedSeat, int clientID) throws RemoteException {

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
                    return new Message(MessageType.ACCEPT_OK);
                } else {
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