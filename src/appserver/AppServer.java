package appserver;

import auxiliary.ConnectionHandler;


/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class AppServer {
    final static int NUM_DBSERVER = 2;
    final static int NUM_THEATERS = 1500;


    public static void main(String args[]) throws Exception {

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
        String zkAddress = zkIP + ":" + zkPort;
        ConnectionHandler connector = new ConnectionHandler(zkAddress, ConnectionHandler.type.AppServer);
        WideBoxImpl widebox = new WideBoxImpl(connector, NUM_DBSERVER, NUM_THEATERS);

        connector.register(widebox, local_ip);
        System.out.println("AppServer ready");
    }
}
