package dbserver;

import auxiliary.ConnectionHandler;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class DBServer {
	
	// Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync) 
	final static int MODE = 3; 
	final static int NUM_THEATERS = 1500; // not used now, using index insted
    final static int NUM_DBSERVER = 2;

    public static void main(String args[]) throws RemoteException {

        String zkIP = "localhost";
        String zkPort = "";

        if (args.length > 0) {
            //args[0] = own IP
            System.setProperty("java.rmi.server.hostname", args[0]);
            //args[1] = zookeeper IP
            zkIP = args[1];
            //args[2] = zookeeper Port
            zkPort = args[2];
        }

        ConnectionHandler connector = new ConnectionHandler(zkIP + ":" + zkPort, ConnectionHandler.type.DBServer);
        DBServerImpl dbServer = null;
        try {
            int dbServerID = connector.getNrOfNodesOnPath("/zookeeper");
            dbServer = new DBServerImpl(MODE,dbServerID*NUM_THEATERS/NUM_DBSERVER,(dbServerID+1)*NUM_THEATERS/NUM_DBSERVER);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        connector.register(dbServer);
        System.out.println("DBServer ready");
    }
}