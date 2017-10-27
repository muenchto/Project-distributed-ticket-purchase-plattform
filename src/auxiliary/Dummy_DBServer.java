package auxiliary;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class Dummy_DBServer {

    public static void main(String args[]) throws Exception {

        int num_theaters = 1500;
        DataStorageImpl dbServer = new DataStorageImpl(num_theaters);

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
