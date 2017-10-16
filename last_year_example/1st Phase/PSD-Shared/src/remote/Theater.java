package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Theater extends Remote {

	public String getName() throws RemoteException;
	public Seat[][] getSeats() throws RemoteException;
	
}
