package appserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import auxiliary.ConnectionHandler;
import auxiliary.LoadBalancerIF;
import auxiliary.WideBoxIF;

public class LoadBalancerImpl extends UnicastRemoteObject implements LoadBalancerIF {

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
