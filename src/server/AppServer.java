package server;

import auxiliary.ConnectionHandler;


/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class AppServer {
    final static int NUM_DBSERVER = 2;


    public static void main(String args[]) throws Exception {

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
        String zkAddress = zkIP + ":" + zkPort;
        ConnectionHandler connector = new ConnectionHandler(zkAddress, ConnectionHandler.type.AppServer);
        WideBoxImpl widebox = new WideBoxImpl(connector, NUM_DBSERVER);

        connector.register(widebox);
        System.out.println("AppServer ready");
    }
}
