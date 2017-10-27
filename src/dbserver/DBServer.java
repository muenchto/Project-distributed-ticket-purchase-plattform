package dbserver;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.TreeSet;

import auxiliary.*;
import auxiliary.Seat.SeatStatus;

public class DBServer extends UnicastRemoteObject implements DataStorageIF {

	protected DBServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7420422297302142902L;
	final static String DBFILENAME = "dbfile.txt";
	final static String LOGFILENAME = "logfile.txt";
	
	public Theater[] theaters = new Theater[1500];
	
	
	
	
	public static void main(String[] args) throws RemoteException {{
			System.out.println("Starting server");
			// CORRECT RMI TO SERVE THE METHODES IMPLEMETED HERE
			//	Registry registry = LocateRegistry.createRegistry(5099);
			//	registry.rebind("DBServer", DBServer);
		}
		
		
		
		
		
		
		
		
		
		
	}



	@Override
	public String[] getTheaterNames() {
		String[] names = null;
		for (int i=0;i<theaters.length;i++) {
			names[i]=theaters[i].theaterName;
		}
		return names;
	}

	@Override
	public Theater getTheater(int theaterName) {
		return theaters[theaterName];
	}

	@Override
	public boolean purchase(int theater, Seat seat) {
		if (theaters[theater].seats[seat.rowNr][seat.colNr].status==SeatStatus.FREE||
				theaters[theater].seats[seat.rowNr][seat.colNr].status==SeatStatus.RESERVED)
			theaters[theater].seats[seat.rowNr][seat.colNr].status=SeatStatus.OCCUPIED;
		else
			return false;
		return true;
	}

	@Override
	public boolean cancelReserve(int theater, Seat seat) {
		if (theaters[theater].seats[seat.rowNr][seat.colNr].status==SeatStatus.RESERVED)
			theaters[theater].seats[seat.rowNr][seat.colNr].status=SeatStatus.FREE;
		else
			return false;
	return true;
	}

	@Override
	public boolean reserveSeat(int theater, Seat seat) {
		if (theaters[theater].seats[seat.rowNr][seat.colNr].status==SeatStatus.FREE)
			theaters[theater].seats[seat.rowNr][seat.colNr].status=SeatStatus.RESERVED;
		else
			return false;
		return true;
	}

	@Override
	public Seat reserveSeat(int theater) {
		//TODO return a random free seat from the theater
		return null;
	}

	@Override
	public boolean occupySeat(String theaterName, Seat theaterSeat) throws RemoteException {
		return false;
	}

}

