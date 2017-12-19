package server;

import auxiliary.Message;
import auxiliary.Seat;
import auxiliary.WideBoxIF;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * PSD Project - Phase 2
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class LoadBalancer {

    public static void main(String args[]) throws Exception {

        System.setProperty("java.rmi.server.hostname", args[2]);
        System.out.println(System.getProperty("java.rmi.server.hostname"));

        Registry registry;
        LoadBalancerImpl loadbalancer;
        try {
            // Bind the remote object's stub in the registry
            if (args.length > 0) {
                registry = LocateRegistry.createRegistry(5000);
                
                loadbalancer = new LoadBalancerImpl(args[0], args[1]);
            } else {
                registry = LocateRegistry.getRegistry(5000);
                String localhost = "127.0.0.1";
                loadbalancer = new LoadBalancerImpl(localhost, localhost);
            }

            registry.rebind("WideBoxServer", loadbalancer);
            System.err.println("LoadBalancer ready");

        } catch (Exception e) {
            System.err.println("LoadBalancer exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static class LoadBalancerImpl extends UnicastRemoteObject implements WideBoxIF{

        WideBoxIF wideboxStub1;
        WideBoxIF wideboxStub2;

        //cache the Theater Names
        String[] theaterNames = null;


        protected LoadBalancerImpl(String appS1IP, String appS2IP) throws RemoteException {

            try {
            	Registry registryAppS1 = LocateRegistry.getRegistry(appS1IP, 5000);
            	Registry registryAppS2 = LocateRegistry.getRegistry(appS2IP, 5000);
                this.wideboxStub1 = (WideBoxIF) registryAppS1.lookup("AppServer1");
                this.wideboxStub2 = (WideBoxIF) registryAppS2.lookup("AppServer2");
            } catch (RemoteException e1) {
                e1.printStackTrace();
            } catch (NotBoundException e1) {
                e1.printStackTrace();
            }
        }

		private WideBoxIF forwardServer(String theaterName) {
            int theaterNR = Integer.parseInt(theaterName.substring(9));
            if (theaterNR < theaterNames.length/2) {
               return this.wideboxStub1;
            }
            else {
                return this.wideboxStub2;
            }
        }

        @Override
        public String[] getNames() throws RemoteException {
            if (theaterNames != null) {
                return theaterNames;
            }
            else {
                String[] theaters1 = wideboxStub1.getNames();
                String[] theaters2 = wideboxStub2.getNames();
                int aLen = theaters1.length;
                int bLen = theaters1.length;
                theaterNames = new String[aLen+bLen];
                System.arraycopy(theaters1, 0, theaterNames, 0, aLen);
                System.arraycopy(theaters2, 0, theaterNames, aLen, bLen);
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
