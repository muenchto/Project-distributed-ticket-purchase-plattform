package server;

import auxiliary.*;
import client.Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * Created by tobiasmuench on 12.10.17.
 */
public class WideBoxImpl extends UnicastRemoteObject implements WideBoxIF {

    private Registry registry;
    private DataStorageIF dataStorageStub;

    private HashMap<Integer, ClientData> clientsList;

    public WideBoxImpl() throws RemoteException {

        try {
            //TODO: find the registry
            registry = LocateRegistry.getRegistry(null);
            dataStorageStub = (DataStorageIF) registry.lookup("DBServer");

            System.err.println("WideBoxImpl found DBServer");

            //initialize the HashMap for the clients, set to maximum number of clients according to the assignment
            HashMap<Integer, ClientData> clientsList = new HashMap<Integer, ClientData>(100000);
        } catch (Exception e) {
            System.err.println("WideBoxImpl exception: " + e.toString());
            e.printStackTrace();
        }
    }


    public String[] getNames() throws RemoteException {
        return new dataStorageStub.getTheaterNames();
    }

    public Message query(String theaterName) throws RemoteException {
        Theater theater = dataStorageStub.getTheater(theaterName);
        if (theater.status() == TheaterStatus.FULL) {
            return new Message(MessageType.FULL);
        }
        else {
            //create ClientID
            //reserve a seat
            // insert clientID and reserved theater.seats.seat and timer to a internal DS
            return new Message(MessageType.AVAILABLE, theater.seats, clientID);
        }
    }

    public Message reserve(Seat seat, int clientID) throws RemoteException {
        ClientData client = validateClient(clientID);
        if (client == null) {
            return new Message(MessageType.ERROR);
        }

        Theater theater = dataStorageStub.getTheater(client.theaterName);
        Seat new_seat = theater.reserveSeat;
        if (new_seat != null) {
            theater.freeSeat(client.seat);
            client.seat = new_seat;
            client.timer.start;
            return new Message(MessageType.AVAILABLE, theater.seats, clientID);
        }
        else {
            return new Message(MessageType.AVAILABLE, theater.seats, clientID);
        }
    }

    public Message accept(int clientID) throws RuntimeException {
        // check if clientID is existing (maybe check also timer of the clientID)
        return null;
    }

    public Message cancel(int clientID) throws RuntimeException {
        return null;
    }

    private ClientData validateClient(int clientID){
        if (!clientsList.containsKey(clientID)){
            return null;
        }
        else {
            ClientData client = clientsList.get(clientID);
            if (client.timer < 15000) {
                return null;
            }
            else {
                return client;
            }
        }


    }
}