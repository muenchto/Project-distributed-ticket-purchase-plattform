package appserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import auxiliary.ConnectionHandler;
import auxiliary.LoadBalancerIF;
import auxiliary.WideBoxIF;

public class LoadBalancerImpl extends UnicastRemoteObject implements LoadBalancerIF, ConnectionHandler.ConnectionWatcher {

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
				System.out.println("LOADBALANCER:  connected to AppServer" + i);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public HashMap<String, String[]> getNames() throws RemoteException {
		if (!theaterNames.isEmpty()) {
			System.out.println("LOADBALANCER: getNames: cached list returned" );
			return theaterNames;
		} else {
			// List<String> theaterList = new ArrayList<>();
			System.out.println("LOADBALANCER: getNames: build new List of theaterNames: ");
			int i = 0;
			for (WideBoxIF aS : appserverList) {
				// theaterList.addAll(Arrays.asList(aS.getNames()));
				theaterNames.put("appserver" + i, aS.getNames());
				System.out.println("appserver" + i + ": " + Arrays.toString(theaterNames.get("appserver" + i)));
				i++;
			}
			return theaterNames;
		}
	}

	@Override
	public void connectionLost(String znode) {

	}
}
