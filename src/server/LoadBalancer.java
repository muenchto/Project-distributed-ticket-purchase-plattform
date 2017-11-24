package server;

import auxiliary.Message;
import auxiliary.Seat;
import auxiliary.WideBoxIF;

import java.net.InetAddress;
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

        System.setProperty("java.rmi.server.hostname", args[2]);
        System.out.println(System.getProperty("java.rmi.server.hostname"));

        Registry registry;
        Registry registryAppS1;
        Registry registryAppS2;
        LoadBalancerImpl loadbalancer;
        try {
            // Bind the remote object's stub in the registry
            if (args.length > 0) {
                registry = LocateRegistry.createRegistry(5000);
//                registryAppS1 = LocateRegistry.getRegistry(args[0], 5000);
//                registryAppS2 = LocateRegistry.getRegistry(args[1], 5000);

                //try {
//                    WideBoxIF wideboxStub1 = (WideBoxIF) registryAppS1.lookup("AppServer1");
//                    WideBoxIF wideboxStub2 = (WideBoxIF) registryAppS2.lookup("AppServer2");
//                    loadbalancer = new LoadBalancerImpl();
//                    loadbalancer.setAppServer1(wideboxStub1);
//                    loadbalancer.setAppServer2(wideboxStub2);
               
                /*} catch (RemoteException e1) {
                    e1.printStackTrace();
                } catch (NotBoundException e1) {
                    e1.printStackTrace();
                }*/
                
                loadbalancer = new LoadBalancerImpl(args[0], args[1]);
                //loadbalancer = new LoadBalancerImpl(registryAppS1, registryAppS2);
            } else {
                registry = LocateRegistry.getRegistry(5000);
                loadbalancer = new LoadBalancerImpl(registry, registry);
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

        protected LoadBalancerImpl(Registry registryAppS1, Registry registryAppS2) throws RemoteException {

            try {
                this.wideboxStub1 = (WideBoxIF) registryAppS1.lookup("AppServer1");
                this.wideboxStub2 = (WideBoxIF) registryAppS2.lookup("AppServer2");
            } catch (RemoteException e1) {
                e1.printStackTrace();
            } catch (NotBoundException e1) {
                e1.printStackTrace();
            }
        }
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

        public void setAppServer2(WideBoxIF wideboxStub2) {
        	this.wideboxStub2 = wideboxStub2;			
		}

		public void setAppServer1(WideBoxIF wideboxStub1) {
			this.wideboxStub1 = wideboxStub1;
		}

		public LoadBalancerImpl() throws RemoteException {
		}

		private WideBoxIF forwardServer(String theaterName) {
			System.out.println("im forwarding to "+theaterName);
            int theaterNR = Integer.parseInt(theaterName.substring(9));
            if (theaterNR < theaterNames.length/2) {
            System.out.println("im on the if");
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
            	System.out.println("getNames");
                 theaterNames = wideboxStub1.getNames();
                 System.out.println(theaterNames[1]);
                 return theaterNames;
            }
        }

        @Override
        public Message query(String theaterName) throws RemoteException {
        	System.out.println("im on the query LOADBALANCER");
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
