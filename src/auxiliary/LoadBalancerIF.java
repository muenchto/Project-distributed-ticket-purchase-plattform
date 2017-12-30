package auxiliary;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface LoadBalancerIF extends Remote{
	public HashMap<String, String[]> getNames() throws RemoteException; 
}
