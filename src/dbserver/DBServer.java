package dbserver;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class DBServer {

    public static void main(String args[]) throws Exception {
    	// Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync) future use
    	int mode = 0;
        int num_theaters = 1500;
        DBServerImpl dbServer = new DBServerImpl(num_theaters,mode);

        Registry registry;
        try {
            // Bind the remote object's stub in the registry
            registry = LocateRegistry.createRegistry(5000);
            registry.rebind("dbServer", dbServer);

            System.err.println("dbServer ready");
        } catch (Exception e) {
            System.err.println("dbServer exception: " + e.toString());
            e.printStackTrace();
        }
    }

}

