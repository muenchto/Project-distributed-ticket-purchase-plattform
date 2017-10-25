package auxiliary;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by tobiasmuench on 24.10.17.
 */
public interface WideBoxIF extends Remote{
    String[] getNames() throws RemoteException;
    Message query(String theaterName) throws RemoteException;
    Message reserve(Seat seat, int clientID) throws RemoteException;
    Message accept(int clientID) throws RemoteException;
    Message cancel(int clientID) throws RemoteException;
}
