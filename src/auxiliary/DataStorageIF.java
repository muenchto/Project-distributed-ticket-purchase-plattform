package auxiliary;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */

public interface DataStorageIF extends Remote{

		String[] getTheaterNames() throws RemoteException;
		Theater getTheater(String theaterName) throws RemoteException;
		boolean occupySeat(String theaterName, Seat theaterSeat) throws RemoteException;
		void killServer() throws RemoteException;
		//boolean isSeatFree(String theaterName, Seat theaterSeat) throws RemoteException;
		
		//Methods for communication updates among dbservers **only** !!!
		/*
		boolean updateSoldSeat (String theaterName, Seat theaterSeat) throws RemoteException;
		boolean[] updateSoldSeat(String theaterName[], Seat theaterSeat[]) throws RemoteException;
		*/
		//insted of boolean use int initially for debug propose (to use several error codes)
		int updateSoldSeat (String theaterName, Seat theaterSeat) throws RemoteException;
		int[] updateSoldSeat(String theaterName[], Seat theaterSeat[]) throws RemoteException;
		boolean sendSnapshot (ConcurrentHashMap<String, Theater> snapShot) throws RemoteException;
		
	}
