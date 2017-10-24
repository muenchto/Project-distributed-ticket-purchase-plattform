package server;

import auxiliary.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by tobiasmuench on 12.10.17.
 */
public class WideBoxImpl extends UnicastRemoteObject implements WideBoxIF {

    private Registry registry;
    private DataStorageIF dataStorageStub;
    public WideBoxImpl() throws RemoteException {

        try {
            //TODO: find the registry
            registry = LocateRegistry.getRegistry(null);
            dataStorageStub = (DataStorageIF) registry.lookup("DBServer");

            System.err.println("WideBoxImpl found DBServer");
        } catch (Exception e) {
            System.err.println("WideBoxImpl exception: " + e.toString());
            e.printStackTrace();
        }
    }


    public String[] getNames() throws RemoteException {
        return new dataStorageStub.getTheaterNames();
    }

    public Message query(String theaterName) throws RemoteException {
        return null;
    }

    public Message reserve(Seat seat, int clientID) throws RemoteException {
        return null;
    }

    public Message accept(int clientID) throws RuntimeException {
        return null;
    }

    public Message cancel(int clientID) throws RuntimeException {
        return null;
    }
}