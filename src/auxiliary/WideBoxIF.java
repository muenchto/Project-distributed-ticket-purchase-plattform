package auxiliary;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public interface WideBoxIF extends Remote{
    String[] getNames() throws RemoteException;
    Message query(String theaterName) throws RemoteException;
    Message reserve(String theaterName, Seat old_seat, Seat new_seat, int clientID) throws RemoteException;
    Message accept(String theaterName, Seat seat, int clientID) throws RemoteException;
    Message cancel(String theaterName, Seat seat, int clientID) throws RemoteException;
    void killServer() throws RemoteException;
}
