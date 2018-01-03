package auxiliary;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface LoadBalancerIF extends Remote{
	HashMap<String, String[]> getNames() throws RemoteException;
	void killServer() throws RemoteException;
}
