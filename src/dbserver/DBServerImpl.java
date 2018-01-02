package dbserver;

import auxiliary.ConnectionHandler;
import auxiliary.DataStorageIF;
import auxiliary.Seat;
import auxiliary.Seat.SeatStatus;
import auxiliary.Theater;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DBServerImpl extends UnicastRemoteObject implements DataStorageIF {
	private static final long serialVersionUID = -7370182827432554702L;
	public ConcurrentHashMap<String, Theater> theaters ;
	public ConcurrentHashMap<String, Theater> theatersBackup;

	public  Storage storageFile;
	final static String DBFILENAME = "DBfile.txt";
	final static String LOGFILENAME = "LOGfile.txt";
	/*
	public Storage storageBkFile;
	final static String DBFILENAMEBACKUP = "DB_BACKUPfile.txt";
	final static String LOGFILENAMEBACKUP = "LOG_BACKUPfile.txt";
	*/
	private int firstTheater;
	private int lastTheater;
	private int firstBackTheater;
	private int lastBackTheater;
	final static int MAXOPERATIONS=100; //limit operations to create snapshot
	// Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync) future use
	public int mode;
	public int NUM_SERVERS;
	public int SERVER_ID;
	public int NUM_THEATERS;
	private int errors;

	private ConnectionHandler connector;
	private DataStorageIF backupServerStub;
	private DataStorageIF primaryServerStub;

	private int opCountBack = 0;
	private int opCount=0;

	private boolean flag_bkserver_down;


	public DBServerImpl(int ID, int NUM_DBSERVER, ConnectionHandler connector, int writingMode, int NUM_THEATERS) throws IOException{

		this.SERVER_ID = ID;
		this.NUM_SERVERS = NUM_DBSERVER;
		mode=writingMode;


		this.connector = connector;

		//For sharding. Array with the starting and end index of the piece of data
		//that the replica is responsible, as a primary and as a backup of other.
		int succesiveID = Math.floorMod((SERVER_ID + 1), NUM_SERVERS);
		this.firstTheater = SERVER_ID * NUM_THEATERS / NUM_SERVERS;
		if (SERVER_ID == NUM_SERVERS -1) {
			this.lastTheater = NUM_THEATERS;
		} else {
			this.lastTheater = succesiveID * NUM_THEATERS / NUM_SERVERS;
		}

		int primary_Server_ID = Math.floorMod((SERVER_ID - 1), NUM_SERVERS);
		this.firstBackTheater = primary_Server_ID * NUM_THEATERS / NUM_SERVERS;
		if (primary_Server_ID == NUM_SERVERS -1) {
			this.lastBackTheater = NUM_THEATERS;
		} else {
			this.lastBackTheater = this.firstTheater;
		}

		storageFile = new Storage (ID, firstTheater, lastTheater, firstBackTheater, mode);
		//storageBkFile =  new Storage (DBFILENAMEBACKUP,LOGFILENAMEBACKUP);

		//if there is a db file, load the file to memory hashmap 
		//if there isn't an existant db file, create clean theaters hashmap and make first dump to create a new file snapshot
		if (storageFile.existentDBfile() && false) {
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
			}
			System.out.println("created "+theaters.size()+" theaters from "+theaters.get("TheaterNr"+firstTheater).theaterName + " until "+theaters.get("TheaterNr"+(lastTheater-1)).theaterName);

			//dump newly createad hashmap to file
			storageFile.saveToFile(theaters);
		}

		//BACKUP DATA
		if (storageFile.existentDBbackfile()) {
			System.out.println("DB backup file present, loading DB");
			//Creation of the theaters hashmap
			theatersBackup = storageFile.loadDBBackfile();
		}
		//if there isn't an existant db file, create clean theaters hashmap and make first dump to create a new file snapshot
		else {
			//Creation of the theaters hashmap
			System.out.println("DB backup file NOT present, creating new hashmap");
			theatersBackup = new ConcurrentHashMap<String, Theater>();
			for (int i = firstTheater; i < lastTheater; i++) {
				theatersBackup.put("TheaterNr" + i,  new  Theater("TheaterNr" + i));
				//System.out.println(" nome do teatro "+theaters.get("TheaterNr"+i).theaterName+" - "+theaters.get("TheaterNr"+i).toString()+" adicionado"); //DEBUG USE
			}
			//dump newly createad hashmap to file
			storageFile.saveToBackFile(theatersBackup);
		}




        connector.register(this);
        //if this db server is the last one, he has to inform the dbserver0 that he is alive an will have dbserver0 as backup
        if (ID == NUM_DBSERVER-1) {
            primaryServerStub = (DataStorageIF) connector.get("dbserver" + (ID-1), "/dbserver");
            System.out.println("Connection to PRIMARY SERVER" + (ID-1)+" established");
            primaryServerStub.notifyBackupAlive(ID);
            backupServerStub = (DataStorageIF) connector.get("dbserver0", "/dbserver");
            System.out.println("Connection to BACKUP SERVER0 established");
			flag_bkserver_down = false;
            backupServerStub.notifyPrimaryAlive(ID);
        }
        // if dbserver is not the first one (and not the last one), only connect to its primary server and notify this one that the backup server is up
        else if (ID > 0){
            primaryServerStub = (DataStorageIF) connector.get("dbserver" + (ID-1), "/dbserver");
            System.out.println("Connection to PRIMARY SERVER" + (ID-1)+" established");
            primaryServerStub.notifyBackupAlive(ID);
        }

		if(ID > 0){
			try {
				theatersBackup = primaryServerStub.getSnapshot();
			}
			catch (RemoteException e) {
				System.err.println("DBSERVER"+ID+": primary server connection down");
				//e.printStackTrace();
			}
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
		if(!theaters.containsKey(theaterName)) {
			System.out.println("DBServer cannot find theater " + theaterName +
					".This DBServer is only responsible for " + firstTheater + " to " + lastTheater + " theaters");
		}
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
				if (!flag_bkserver_down) {
					// update the replica backup
					updateBackup(theaterName,theaterSeat);
				}
				//log operation to file
				storageFile.buySeat(theaterName,theaterSeat);
				// to count operations to at x operations, save memory to file and delete log file
				countOperation();
			}
			return true;
		}
		else if(theatersBackup.get(theaterName).seats[theaterSeat.rowNr-'A'][theaterSeat.colNr].status==SeatStatus.FREE) {
			synchronized(this){
				//UPDATE Hashmap
				theatersBackup.get(theaterName).occupySeat(theaterSeat);
				//log operation to file
				storageFile.buySeatInBackup(theaterName, theaterSeat);
				// to count operations to at x operations, save memory to file and delete log file
				countOperationback();
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
	public ConcurrentHashMap<String, Theater> getSnapshot() throws RemoteException {
		return theaters;
	}

    @Override
    public void notifyBackupAlive(int backupServerID) throws RemoteException {
        backupServerStub = (DataStorageIF) connector.get("dbserver" + backupServerID, "/dbserver");
        System.out.println("Connection to BACKUP SERVER" + backupServerID + " established");
		flag_bkserver_down = false;
    }

	@Override
	public void notifyPrimaryAlive(int id) throws RemoteException {
		primaryServerStub = (DataStorageIF) connector.get("dbserver" + (id), "/dbserver");
		System.out.println("Connection to PRIMARY SERVER" + (id)+" established");
		if (SERVER_ID == 0 && id == NUM_SERVERS)
			try {
				theatersBackup = primaryServerStub.getSnapshot();
			}
			catch (RemoteException e) {
				System.err.println("DBSERVER"+id+": primary server connecting down");
				e.printStackTrace();
			}
	}

    // Auxiliary methods ******************************************************
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

	synchronized private void  updateBackup(String theaterName, Seat theaterSeat) {
		try {
			int resp=backupServerStub.updateSoldSeat(theaterName, theaterSeat);
			switch (resp) {
				case -2 :
					System.out.println("FROM BACKUP: theater found on primarys theater, something it's not alright ");
					break;
				case -1 :
					System.out.println("FROM BACKUP: theater does not exist in theaterbackup, something it's not alright ");
					break;
				case 0 :
					System.out.println("FROM BACKUP: seat not available");
					break;
				case 1 :
					System.out.println("FROM BACKUP: operation sucesseful");
					break;
			}

		} catch (RemoteException e) {
			System.err.println("DBSERVER: backup server connecting down");
			flag_bkserver_down = true;
			//e.printStackTrace();
		}
	}


	private synchronized void countOperationback() {
		opCountBack++;
		if (opCountBack>MAXOPERATIONS) {
			try {
				storageFile.saveToBackFile(theatersBackup);
			} catch (IOException e) {
				System.err.println("DBSERVER: Error in couting operations");
				e.printStackTrace();
			}
			opCountBack=0;
		}
	}

}