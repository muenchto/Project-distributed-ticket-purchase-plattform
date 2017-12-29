package server;

import auxiliary.ConnectionHandler;
import auxiliary.LoadBalancerIF;
import auxiliary.Message;
import auxiliary.Seat;
import auxiliary.TheaterStatus;
import auxiliary.WideBoxIF;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
			// args[0] = own IP
			System.setProperty("java.rmi.server.hostname", args[0]);

			// args[1] = zookeeper IP
			zkIP = args[1];
			// args[2] = zookeeper Port
			zkPort = args[2];
		}
		String zkAddress = zkIP + ":" + zkPort;
		ConnectionHandler connector = new ConnectionHandler(zkAddress, ConnectionHandler.type.LoadBalancer);
		LoadBalancerImpl loadBalancer = new LoadBalancerImpl(connector);

		connector.register(loadBalancer);
		System.out.println("LoadBalancer ready");
	}

	public static class LoadBalancerImpl extends UnicastRemoteObject implements LoadBalancerIF {

		ArrayList<WideBoxIF> appserverList;
		int num_appserver;

		// cache the Theater Names
		HashMap<String, String[]> theaterNames;

		protected LoadBalancerImpl(ConnectionHandler connector) throws RemoteException {

			theaterNames = new HashMap<>();
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

		@Override
		public HashMap<String, String[]> getNames() throws RemoteException {
			if (!theaterNames.isEmpty()) {
				return theaterNames;
			} else {
				// List<String> theaterList = new ArrayList<>();
				int i = 0;
				for (WideBoxIF aS : appserverList) {
					// theaterList.addAll(Arrays.asList(aS.getNames()));
					theaterNames.put("appserver" + i, aS.getNames());
					i++;
				}
				System.out.println(theaterNames.toString());
				return theaterNames;
			}
		}
	}
}