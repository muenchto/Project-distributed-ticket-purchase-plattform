package dbserver;

import auxiliary.ConnectionHandler;
import auxiliary.WideBoxConfigHandler;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class DBServer {
	
	// Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync) 
    static int MODE = 3;

    public static void main(String args[]) throws RemoteException {

        WideBoxConfigHandler configfile = new WideBoxConfigHandler();
        System.out.println(configfile.toString());

        int NUM_DBSERVERS = configfile.getNum_servers();
        int NUM_THEATERS = configfile.getNum_theaters();

        String local_ip = "127.0.0.1";
        String zkIP = "localhost";
        String zkPort = "";

        if (args.length > 0) {
            local_ip = args[0];
            System.setProperty("java.rmi.server.hostname", local_ip);
            //args[1] = zookeeper IP
            zkIP = args[1];
            //args[2] = zookeeper Port
            zkPort = args[2];
        }

        ConnectionHandler connector = new ConnectionHandler(zkIP + ":" + zkPort, ConnectionHandler.type.DBServer);
        DBServerImpl dbServer = null;
        try {
            int dbServerID = connector.getNrOfNodesOnPath("/dbserver");
            dbServer = new DBServerImpl(dbServerID, local_ip, NUM_DBSERVERS, connector, MODE, NUM_THEATERS);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        System.out.println("DBServer ready");
    }
}