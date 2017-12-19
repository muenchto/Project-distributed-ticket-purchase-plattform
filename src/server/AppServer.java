package server;

import java.net.InetAddress;
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

        //args[0] = own IP
        //args[1] = zookeeper IP

        if (args.length > 0) {
            System.setProperty("java.rmi.server.hostname", args[0]);
        }

        Registry registry;
        String ZKadress;
        try {
            //case distributed
            if (args.length > 0) {
                registry = LocateRegistry.createRegistry(5000);
                ZKadress = args[1];
            }
            //case local
            else {
                registry = LocateRegistry.getRegistry(5000);
                ZKadress = "localhost";
            }

            WideBoxImpl widebox = new WideBoxImpl(ZKadress);
            registry.rebind("AppServer" + widebox.getNumServersAtStart(), widebox);
            System.out.println("AppServer "+widebox.getNumServersAtStart()+" ready");

        } catch (Exception e) {
            System.err.println("AppServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
