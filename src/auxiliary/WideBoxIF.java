package auxiliary;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by tobiasmuench on 24.10.17.
 */
public interface WideBoxIF extends Remote{
    String[] getNames() throws RemoteException;
    Message query(String theaterName) throws RemoteException;
    Message reserve(Seat old_seat, Seat new_seat, int clientID) throws RemoteException;
    Message accept(Seat seat, int clientID) throws RemoteException;
    Message cancel(Seat seat, int clientID) throws RemoteException;
}
