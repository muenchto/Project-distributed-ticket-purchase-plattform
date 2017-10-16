package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Seat extends Remote {
		public String getName() throws RemoteException;
		public State getState() throws RemoteException;
		public State setState() throws RemoteException;
}
