package dbserver;

import auxiliary.DataStorageIF;
import auxiliary.Seat;
import auxiliary.Seat.SeatStatus;
import auxiliary.Theater;
import zookeeperlib.ZooKeeperConnection;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DBServerImpl extends UnicastRemoteObject implements DataStorageIF {
	private static final long serialVersionUID = -7370182827432554702L;
	public static ConcurrentHashMap<String, Theater> theaters ;
	public static ConcurrentHashMap<String, Theater> theatersBackup;
	//For sharding. Array with the starting and end index of the piece of data
	//that the replica is responsible, as a primary and as a backup of other.
	public int[] theaterIndexPrim = new int [2];
	public int[] theaterIndexBack = new int [2];
	public static Storage storageFile;
	final static String DBFILENAME = "DBfile.txt";
	final static String LOGFILENAME = "LOGfile.txt";
	public static Storage storageBkFile;
	final static String DBFILENAMEBACKUP = "DB_BACKUPfile.txt";
	final static String LOGFILENAMEBACKUP = "LOG_BACKUPfile.txt";
	private int firstTheater;
	private int lastTheater;
	private int opCount=0;
	final static int MAXOPERATIONS=100; //limit operations to create snapshot
	// Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync) future use
	public int mode;
	private int errors;

	private int numServersAtStart;

	public DBServerImpl(int writingMode, int firstTheater, int lastTheater ) throws IOException{
		mode=writingMode;
		storageFile = new Storage (DBFILENAME, LOGFILENAME, firstTheater, lastTheater, writingMode);
		//storageBkFile =  new Storage (DBFILENAMEBACKUP,LOGFILENAMEBACKUP);
		this.firstTheater=firstTheater;
		this.lastTheater=lastTheater;


		//if there is a db file, load the file to memory hashmap 
		//if there isn't an existant db file, create clean theaters hashmap and make first dump to create a new file snapshot
		if (storageFile.existentDBfile()) {
			System.out.println("DB file present, loading DB");
			//Creation of the theaters hashmap
			theaters = storageFile.loadDBfile();
		}
		//if there isn't an existant db file, create clean theaters hashmap and make first dump to create a new file snapshot
		else {
			//Creation of the theaters hashmap
			System.out.println("DB file NOT present, creating new hashmap");
			theaters = new ConcurrentHashMap<String, Theater>();
			for (int i = firstTheater; i < lastTheater; i++) {
				theaters.put("TheaterNr" + i,  new  Theater("TheaterNr" + i));
				//System.out.println(" nome do teatro "+theaters.get("TheaterNr"+i).theaterName+" - "+theaters.get("TheaterNr"+i).toString()+" adicionado"); //DEBUG USE
			}
			//dump newly createad hashmap to file
			storageFile.saveToFile(theaters);
		}

	}

	// RMI FUNCTIONS **********************************************************
	@Override
	public synchronized String[] getTheaterNames() throws RemoteException{
		Set<String> keys = theaters.keySet();
		String[] names = (String[]) keys.toArray(new String[keys.size()]);
		return names;	
	}

	@Override
	public synchronized Theater getTheater(String theaterName) throws RemoteException{
		if(!theaters.contains(theaterName))
			System.out.println("DBServer cannot find theater "+theaterName+
					".This DBServer is only responsible for "+firstTheater+" to "+lastTheater+" theaters");
		return theaters.get(theaterName);
	}

	@Override
	//ONLY CALL THIS FUCTION IF EXIST A PRIOR RESERVATION.
	//This validation should be done at appserver 
	public boolean occupySeat(String theaterName, Seat theaterSeat) throws RemoteException{
		System.out.println("DBServerImpl: occupySeat");
		//Theater theater = theaters.get(theaterName).seats
		if(theaters.get(theaterName).seats[theaterSeat.rowNr-'A'][theaterSeat.colNr].status==SeatStatus.FREE) {
			synchronized(this){
				//UPDATE Hashmap
				theaters.get(theaterName).occupySeat(theaterSeat);
				updateBackup(theaterName,theaterSeat);
				//TODO update the replica backup 
				//log operation to file
				storageFile.buySeat(theaterName,theaterSeat);
				// to count operations to at x operations, save memory to file and delete log file
				countOperation();
			}
			return true;
		}
		else {
			errors++;
			System.out.println("DBSERVER: OCCUPY ERROR [" + errors + "]: " + theaterName + " " + theaterSeat.getSeatName() + " seat already taken.");
			return false;
		}
	}


	



	//only needed in case of lazy synchronization
	/*public synchronized boolean isSeatFree(String theaterName, Seat theaterSeat) throws RemoteException {
		if (theaters.get(theaterName).seats[theaterSeat.rowNr-'A'][theaterSeat.colNr].status == SeatStatus.FREE) {
			return true;
		}
		return false;
	}*/

	@Override
	public void killServer() throws RemoteException {
		System.exit(0);

	}


	//one update
	@Override
	public int updateSoldSeat(String theaterName, Seat theaterSeat) throws RemoteException {		
		int response;
		response=updateSoldSeatAux( theaterName,  theaterSeat);
		return response;
	}

	//set of updates
	@Override
	public int[] updateSoldSeat(String[] theaterName, Seat[] theaterSeat) throws RemoteException {
		int[] response = new int[theaterName.length];
		for (int i=0; i<theaterName.length;i++) {
			response[i]=updateSoldSeatAux( theaterName[i],  theaterSeat[i]);
		}
		return response;

	}

	
	@Override
	public ConcurrentHashMap<String, Theater> Snapshot() throws RemoteException {
		return theaters;
	}

	
	// Auxiliary methods ******************************************************

	public int getNumServersAtStart() {
		return numServersAtStart;
	}
	

	private synchronized int updateSoldSeatAux(String theaterName, Seat theaterSeat) {
		if (theatersBackup.contains(theaterName)) {
			if(theatersBackup.get(theaterName).occupySeat(theaterSeat))
				// operation sucesseful
				return 1;
			else
				// seat not available
				return 0;
		}
		else
			if (theaters.contains(theaterName))
				//theater found on primarys theater, something it's not alright 
				//This should not happen but if it happens you know
				return -2;
			else
				//theater does not exist in theaterbackup, something it's not alright 
				return -1;
	}

	//Count operations to at 100 operations, save memory to file and delete log file
	private synchronized void countOperation() {
		opCount++;
		if (opCount>MAXOPERATIONS) {
			try {
				storageFile.saveToFile(theaters);
			} catch (IOException e) {
				System.err.println("DBSERVER: Error in couting operations");
				e.printStackTrace();
			}
			opCount=0;
		}		
	}

	private void updateBackup(String theaterName, Seat theaterSeat) {
		//TODO
	}


}