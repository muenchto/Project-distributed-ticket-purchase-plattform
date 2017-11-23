package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class AppServer {

    public static void main(String args[]) throws Exception {


        Registry registry;
        try {
            if (args.length > 1) {
                registry = LocateRegistry.createRegistry(5000);
            }
            else {
                registry = LocateRegistry.getRegistry(5000);
            }

            WideBoxImpl widebox;
            String dbServerIP;
            if (args.length > 1) {
                dbServerIP = args[1];
            }
            else {
                dbServerIP = null;
            }
            if (args[0].equals("1")) {
                widebox = new WideBoxImpl(dbServerIP);
                registry.rebind("AppServer1", widebox);
                System.err.println("AppServer1 ready");
            }
            else {
                widebox = new WideBoxImpl(dbServerIP);
                registry.rebind("AppServer2", widebox);
                System.err.println("AppServer2 ready");
            }




        } catch (Exception e) {
            System.err.println("AppServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
