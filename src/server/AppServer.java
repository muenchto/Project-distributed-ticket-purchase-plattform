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
            if (args.length > 0) {
                registry = LocateRegistry.getRegistry(args[0]);
            }
            else {
                registry = LocateRegistry.getRegistry(5000);
            }

            WideBoxImpl widebox;
            String dbServerIP;
            if (args.length > 0) {
                dbServerIP = args[1];
            }
            else {
                dbServerIP = null;
            }
            if (Arrays.asList(registry.list()).contains("AppServer1")) {
                widebox = new WideBoxImpl(dbServerIP);
                registry.rebind("AppServer2", widebox);
                System.err.println("AppServer2 ready");
            }
            else {
                widebox = new WideBoxImpl(dbServerIP);
                registry.rebind("AppServer1", widebox);
                System.err.println("AppServer1 ready");
            }




        } catch (Exception e) {
            System.err.println("AppServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}