package auxiliary;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public interface DataStorageIF extends Remote {

    String[] getTheaterNames() throws RemoteException;
    Theater getTheater(String theaterName) throws RemoteException;
    boolean occupySeat(String theaterName, Seat theaterSeat) throws RemoteException;
}
