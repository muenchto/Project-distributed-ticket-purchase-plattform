package server;

import auxiliary.ConnectionHandler;
import auxiliary.Message;
import auxiliary.Seat;
import auxiliary.WideBoxIF;
import com.sun.tools.javac.util.ArrayUtils;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PSD Project - Phase 2
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class LoadBalancer {

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
        ConnectionHandler connector = new ConnectionHandler(zkAddress, ConnectionHandler.type.LoadBalancer);
        LoadBalancerImpl loadBalancer = new LoadBalancerImpl(connector);

        connector.register(loadBalancer);
        System.out.println("LoadBalancer ready");
    }

    public static class LoadBalancerImpl extends UnicastRemoteObject implements WideBoxIF{

        ArrayList<WideBoxIF> appserverList;
        int num_appserver;

        //cache the Theater Names
        String[] theaterNames = null;


        protected LoadBalancerImpl(ConnectionHandler connector) throws RemoteException {

            num_appserver = connector.getNrOfNodesOnPath("/appserver");
            appserverList = new ArrayList<>(num_appserver);
            try {
                for (int i = 0; i < num_appserver; i++) {
                   appserverList.add((WideBoxIF) connector.get("appserver" + i, "/appserver"));
                    System.out.println("LoadBalancer connected to AppServer" + i);
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

		private WideBoxIF forwardServer(String theaterName) {
            int theaterNR = Integer.parseInt(theaterName.substring(9));
            int num_theaters_per_server = theaterNames.length/num_appserver;

            System.out.println("theaterNames length: "+theaterNames.length +", "+num_appserver +", "+num_theaters_per_server);
            int appServerNr = theaterNR / num_theaters_per_server;
            System.out.println("Forward Theater" + theaterNR + " to appserver" + appServerNr);
            return appserverList.get(appServerNr);
        }

        @Override
        public String[] getNames() throws RemoteException {
            if (theaterNames != null) {
                return theaterNames;
            }
            else {
                List<String> theaterList = new ArrayList<>();
                for (WideBoxIF aS : appserverList) {
                    theaterList.addAll(Arrays.asList(aS.getNames()));
                }
                theaterNames = theaterList.toArray(new String[0]);
                System.out.println(Arrays.toString(theaterNames));
                return theaterNames;
            }
        }

        @Override
        public Message query(String theaterName) throws RemoteException {
            return forwardServer(theaterName).query(theaterName);
        }

        @Override
        public Message reserve(String theaterName, Seat old_seat, Seat new_seat, int clientID) throws RemoteException {
            return forwardServer(theaterName).reserve(theaterName, old_seat, new_seat, clientID);
        }

        @Override
        public Message accept(String theaterName, Seat seat, int clientID) throws RemoteException {
            return forwardServer(theaterName).accept(theaterName, seat, clientID);
        }

        @Override
        public Message cancel(String theaterName, Seat seat, int clientID) throws RemoteException {
            return forwardServer(theaterName).cancel(theaterName, seat, clientID);
        }

        @Override
        public void killServer() throws RemoteException {
        }
    }


}
