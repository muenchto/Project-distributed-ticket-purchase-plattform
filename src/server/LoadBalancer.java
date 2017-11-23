package server;

import auxiliary.Message;
import auxiliary.Seat;
import auxiliary.WideBoxIF;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

/**
 * PSD Project - Phase 2
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class LoadBalancer {

    public static void main(String args[]) throws Exception {

        Registry registry;
        try {
            // Bind the remote object's stub in the registry
            if (args.length > 0) {
                registry = LocateRegistry.getRegistry(args[0]);
            } else {
                registry = LocateRegistry.getRegistry(5000);
            }

            LoadBalancerImpl loadbalancer = new LoadBalancerImpl(registry);


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

        protected LoadBalancerImpl(Registry registry) throws RemoteException {

            try {
                this.wideboxStub1 = (WideBoxIF) registry.lookup("AppServer1");
                this.wideboxStub2 = (WideBoxIF) registry.lookup("AppServer2");
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
                 theaterNames = wideboxStub1.getNames();
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
