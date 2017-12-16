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

        //args[0] = own IP
        //args[1] = zookeeper IP

        if (args.length > 0) {
            System.setProperty("java.rmi.server.hostname", args[0]);
        }

        Registry registry;
        String ZKadress;
        try {
            if (args.length > 0) {
                registry = LocateRegistry.createRegistry(5000);
                ZKadress = args[1];
            }
            else {
                registry = LocateRegistry.createRegistry(5000);
                ZKadress = "127.0.0.1";
            }

            DBServerImpl dbServer = new DBServerImpl(ZKadress, MODE,1,1500);
            registry.rebind("dbServer" + dbServer.getNumServersAtStart(), dbServer);
            System.out.println("DBServer" + dbServer.getNumServersAtStart() + " ready");

        } catch (Exception e) {
            System.err.println("DBServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
/*
if (args.length==1) {
	mode = Integer.parseInt(args[0]);
	if(mode<1&&mode>3) 
	{
		System.out.println("Must execute with an argument between 1 and 3, "
				+ "Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync)");
		System.exit(0);
	}
	else {
		System.out.println("Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync)\n"
				+ "Initiating DBServer in mode "+mode);
	}
}
else
	System.out.println("Using default mode 1 - Buffer");
*/
