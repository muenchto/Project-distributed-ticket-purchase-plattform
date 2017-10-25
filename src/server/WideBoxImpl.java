package server;

import auxiliary.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by tobiasmuench on 12.10.17.
 */
public class WideBoxImpl extends UnicastRemoteObject implements WideBoxIF {

    private Registry registry;
    private DataStorageIF dataStorageStub;

    private ExpiringMap<Integer, ClientData> clientsList;
    private int clientCounter;

    private LinkedHashMap theaters;

    public WideBoxImpl() throws RemoteException {
        System.out.println("Widebox starting");
        this.theaters = new LinkedHashMap<String, Theater>();
        for (int i = 0; i < 1500; i++) {
            this.theaters.put("TheaterNr"+i, new Theater("TheaterNr"+i));
        }
        //initialize the HashMap for the clients, set to maximum number of clients according to the assignment
        this.clientsList = new ExpiringMap<Integer, ClientData>(15, 1);
        clientsList.getExpirer().startExpiring();
        clientCounter = 0;

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
        System.out.println("start query");
        Theater theater = (Theater) this.theaters.get(theaterName);
        if (theater.status == TheaterStatus.FULL) {
            return new Message(MessageType.FULL);
        }
        else {
            int clientID = clientCounter;
            clientCounter++;
            Seat seat = theater.reserveSeat();
            if (seat != null) {
                System.out.println("reserved seat: "+ seat.rowNr+":"+seat.colNr);
            }
            System.out.println("try to put the client data in list: "+clientID+ ", "+theaterName+", "+seat.rowNr+":"+seat.colNr);
            clientsList.put(clientID, new ClientData(clientID, theaterName, seat));
            System.out.println("put succesful");
            return new Message(MessageType.AVAILABLE, theater.seats, clientID);
        }
    }

    public Message reserve(Seat seat,  int clientID) throws RemoteException {
        ClientData client = clientsList.get(clientID);
        if (client == null) {
            return new Message(MessageType.RESERVE_ERROR);
        }

        //Theater theater = dataStorageStub.getTheater(client.theaterName);
        Theater theater = (Theater) this.theaters.get(client.theaterName);
        Seat new_seat = theater.reserveSeat(seat);
        if (new_seat != null) {
            theater.freeSeat(client.seat);
            client.seat = new_seat;
            clientsList.put(clientID, client);
            return new Message(MessageType.AVAILABLE, theater.seats, clientID);
        }
        else {
            return new Message(MessageType.AVAILABLE, theater.seats, clientID);
        }
    }

   public Message accept(int clientID) throws RemoteException {
        ClientData client = clientsList.get(clientID);
        if (client == null) {
            return new Message(MessageType.ACCEPT_ERROR);
        }

        //Theater theater = dataStorageStub.getTheater(client.theaterName);
       Theater theater = (Theater) this.theaters.get(client.theaterName);
        theater.occupySeat(client.seat);
        clientsList.remove(clientID);
        return new Message(MessageType.ACCEPT_OK);
    }

    public Message cancel(int clientID) throws RemoteException {
        ClientData client = clientsList.get(clientID);
        if (client == null) {
            return new Message(MessageType.CANCEL_ERROR);
        }
        else {
            //Theater theater = dataStorageStub.getTheater(client.theaterName);
            Theater theater = (Theater) this.theaters.get(client.theaterName);
            theater.freeSeat(client.seat);
            clientsList.remove(clientID);
            return new Message(MessageType.CANCEL_OK);
        }
    }
}