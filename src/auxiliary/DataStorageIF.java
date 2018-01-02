package auxiliary;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */

public interface DataStorageIF extends Remote{
	//AppServer communication *********************************************
	String[] getTheaterNames() throws RemoteException;
	Theater getTheater(String theaterName) throws RemoteException;
	boolean occupySeat(String theaterName, Seat theaterSeat) throws RemoteException;

	//Fault generattor communication **************************************
	void killServer() throws RemoteException;
	//boolean isSeatFree(String theaterName, Seat theaterSeat) throws RemoteException;

	//DBServer's communication ********************************************
	//Methods for communication updates among dbservers **only** !!!
	//insted of boolean use int initially for debug propose (to use several error codes)
	//updates are only applied to backup hashmap
	int updateSoldSeat (String theaterName, Seat theaterSeat) throws RemoteException;
	int[] updateSoldSeat(String theaterName[], Seat theaterSeat[]) throws RemoteException;
	ConcurrentHashMap<String, Theater> getSnapshot() throws RemoteException;

    void notifyBackupAlive(int backupServerID) throws RemoteException;

	void notifyPrimaryAlive(int NUM_DBSERVER) throws RemoteException;
}
