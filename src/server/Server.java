package server;

/**
 * Created by tobiasmuench on 25.10.17.
 */
import auxiliary.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server {

    public static void main(String args[]) throws Exception {

        WideBoxImpl widebox = new WideBoxImpl();

        Registry registry;
        try {

            // Bind the remote object's stub in the registry
            registry = LocateRegistry.getRegistry(0);
            registry.rebind("WideBoxServer", widebox);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
