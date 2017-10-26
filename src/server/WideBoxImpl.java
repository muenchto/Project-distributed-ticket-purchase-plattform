package server;

import auxiliary.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tobiasmuench on 12.10.17.
 */
public class WideBoxImpl extends UnicastRemoteObject implements WideBoxIF {

    private Registry registry;
    private DataStorageIF dataStorageStub;

    private int clientCounter;

    private ConcurrentHashMap<String, ExpiringMap<String, Integer>> reservedSeats;

    private LinkedHashMap theaters;

    public WideBoxImpl() throws RemoteException {
        System.out.println("Widebox starting");
        this.theaters = new LinkedHashMap<String, Theater>();
        for (int i = 0; i < 1500; i++) {
            this.theaters.put("TheaterNr"+i, new Theater("TheaterNr"+i));
        }
        this.clientCounter = 0;

        this.reservedSeats = new ConcurrentHashMap<String, ExpiringMap<String, Integer>>(1500);

       /* try {
            //TODO: find the registry
            registry = LocateRegistry.getRegistry(0);
            System.out.println("WideBoxImpl got the registry");
            dataStorageStub = (DataStorageIF) registry.lookup("DBServer");

            System.err.println("WideBoxImpl found DBServer");

        } catch (Exception e) {
            System.err.println("WideBoxImpl exception: " + e.toString());
            e.printStackTrace();
        }*/

    }


    public String[] getNames() throws RemoteException {
        //return dataStorageStub.getTheaterNames();
        java.util.Set keys = this.theaters.keySet();
        String[] names = (String[]) keys.toArray(new String[keys.size()]);
        return names;

    }

    public Message query(String theaterName) throws RemoteException {
        //Theater theater = dataStorageStub.getTheater(theaterName);
        Theater theater = (Theater) this.theaters.get(theaterName);
        theater = theater.clone();
        if (theater.status == TheaterStatus.FULL) {
            return new Message(MessageType.FULL);
        }
        else {
            int clientID = clientCounter;
            clientCounter++;

            //if the theater is queried the first time, the name is added to the HasMap
            //and a new ExpiringMap is created for the Seats and the Expirer is started for this seat map
            if (!reservedSeats.containsKey(theaterName)){
                ExpiringMap<String, Integer> expiringSeatMap = new ExpiringMap<String, Integer>(15);
                expiringSeatMap.getExpirer().startExpiring();
                reservedSeats.put(theater.theaterName, expiringSeatMap);
            }

            //add all reserved seats from one theater to the theaterObject as reserved
            for (String s: reservedSeats.get(theaterName).keySet()) {
                theater.setSeatToReserved(s);
            }
            //reserve a new Seat for the client
            Seat seat = theater.reserveSeat();

            //add this seat to the list
            reservedSeats.get(theater.theaterName).put(seat.getSeatName(), clientID);
            System.out.println("QUERY: reservedSeats @ "+theater.theaterName +": "+ reservedSeats.get(theater.theaterName).keySet());

            return new Message(MessageType.AVAILABLE, theater.seats, seat, clientID);
        }
    }

    public Message reserve(String theaterName, Seat old_seat, Seat wish_seat, int clientID) throws RemoteException {

        //Theater theater = dataStorageStub.getTheater(client.theaterName);
        Theater theater = (Theater) this.theaters.get(theaterName);
        theater = theater.clone();

        //add all reserved seats from one theater to the theaterObject as reserved
        for (String s: reservedSeats.get(theaterName).keySet()) {
            theater.setSeatToReserved(s);
        }
        //try to reserve a new Seat for the client
        Seat new_seat = theater.reserveSeat(wish_seat);
        if (new_seat != null) {
            reservedSeats.get(theaterName).put(new_seat.getSeatName(), clientID);

            //when reserve happens before 15 sec timeout, we need to remove the old reserved seat
            if (reservedSeats.get(theaterName).get(old_seat.getSeatName()) != null) {
                if(reservedSeats.get(theaterName).get(old_seat.getSeatName()) == clientID){
                    reservedSeats.get(theaterName).remove(old_seat.getSeatName());
                }

            }
            theater.freeSeat(old_seat);

            return new Message(MessageType.AVAILABLE, theater.seats, new_seat, clientID);
        }
        else {
            return new Message(MessageType.AVAILABLE, theater.seats, old_seat, clientID);
        }
    }

   public Message accept(String theaterName, Seat acceptedSeat, int clientID) throws RemoteException {

       //check if this client has already that seat reserved, if not, return error
       if (reservedSeats.get(theaterName).get(acceptedSeat.getSeatName()) == null ||
               reservedSeats.get(theaterName).get(acceptedSeat.getSeatName()) != clientID) {
           return new Message(MessageType.ACCEPT_ERROR);
       }

       //dataStorageStub.occupySeat(client.theaterName, client.seat);
       Theater theater = (Theater) this.theaters.get(theaterName);

       theater.occupySeat(acceptedSeat);

       reservedSeats.get(theaterName).remove(acceptedSeat.getSeatName());
       return new Message(MessageType.ACCEPT_OK);
    }

    public Message cancel(String theaterName, Seat seat, int clientID) throws RemoteException {
        //check if this client has already that seat reserved, if not, return error
        if (reservedSeats.get(theaterName).get(seat.getSeatName()) == null ||
                reservedSeats.get(theaterName).get(seat.getSeatName()) != clientID) {
            return new Message(MessageType.CANCEL_ERROR);
        }
        else {
            reservedSeats.get(theaterName).remove(seat.getSeatName());
            return new Message(MessageType.CANCEL_OK);
        }
    }
}