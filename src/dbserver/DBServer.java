package dbserver;


import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.WideBoxImpl;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class DBServer {
	
	// Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync) 
	final static int MODE = 3; 
	final static int NUM_THEATERS = 1500; // not used now, using index insted

    public static void main(String args[]) throws RemoteException {
    	
    	
        System.setProperty("java.rmi.server.hostname", args[0]);
        Registry registry;
        try {
            if (args.length > 1) {
                registry = LocateRegistry.createRegistry(5000);
            }
            else {
                registry = LocateRegistry.getRegistry(5000);
            }

           // WideBoxImpl widebox;
            
            /* when the dbserver is divided switch this with the one under
            if (args[0].equals("1")) {
            	DBServerImpl dbServer = new DBServerImpl(MODE,1,750);
                registry.rebind("dbServer1", dbServer);
                System.err.println("DBServer1 ready");
            }
            */
            
            if (args[0].equals("1")) {
            	DBServerImpl dbServer = new DBServerImpl(MODE,1,1500);
                registry.rebind("dbServer1", dbServer);
                System.err.println("DBServer1 ready");
            }
            
            else {
            	DBServerImpl dbServer = new DBServerImpl(MODE,751,1499);
                registry.rebind("dbServer2", dbServer);
                System.err.println("DBServer2 ready");
            }
        

        } catch (Exception e) {
            System.err.println("AppServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}