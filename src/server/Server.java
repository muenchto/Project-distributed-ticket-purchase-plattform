package server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class Server {

    public static void main(String args[]) throws Exception {



        Registry registry;
        try {
            // Bind the remote object's stub in the registry
            registry = LocateRegistry.createRegistry(5000);
            WideBoxImpl widebox;

            if (args.length > 0) {
                widebox = new WideBoxImpl(args[0]);
            }
            else{
                widebox = new WideBoxImpl(null);
            }

            registry.rebind("WideBoxServer", widebox);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
