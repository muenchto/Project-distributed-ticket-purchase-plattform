package dbserver;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;



import auxiliary.*;
import auxiliary.Seat.SeatStatus;

public class DBServerImpl extends UnicastRemoteObject implements DataStorageIF {
	
	private static final long serialVersionUID = -7370182827432554702L;
	public static ConcurrentHashMap<String, Theater> theaters ;
	public static ConcurrentHashMap<String, Theater> theatersBackup;
	//For sharding. Array with the starting and end index of the piece of data
	//that the replica is responsible, as a primary and as a backup of other.
	public int[] theaterIndexPrim = new int [2];
	public int[] theaterIndexBack = new int [2];
	public Storage storageFile;
	
	// Mode=1 (Buffer); Mode=2 (Buffer+Flush); Mode=3 (Buffer+Flush+Sync) future use
	public int mode;

	private int errors;
	//public Storage storageFile = new Storage ("dbfileBack.txt","dblogfileBack.txt"); //FUTURE USE
	
	public DBServerImpl(int num_theaters, int writingMode ) throws IOException{
		mode=writingMode;
		//checkDBfile();
		//checkDBlogfilme();
		for (int i = 0; i < num_theaters; i++) {
			theaters.put("TheaterNr" + i,  new  Theater("TheaterNr" + i));
			System.out.println(" nome do teatro "+theaters.get("TheaterNr"+i).theaterName+" - "+theaters.get("TheaterNr"+i).toString()+" adicionado"); //DEBUG USE
		}
		
/*
		storageFile = new Storage ("dbfile.txt","dblogfile.txt", num_theaters, mode);
		//if there isn't an existant storage file, create clean theaters hashmap and make first dump to create a new file
		if (storageFile.existentDBfile()) {
			//Creation of the theaters hashmap
			theaters = storageFile.loadDBfile();

		}
		else {
			//Creation of the theaters hashmap
			theaters = new ConcurrentHashMap<String, Theater>();
			for (int i = 0; i < num_theaters; i++) {
				theaters.put("TheaterNr" + i,  new  Theater("TheaterNr" + i));
				System.out.println(" nome do teatro "+theaters.get("TheaterNr"+i).theaterName+" - "+theaters.get("TheaterNr"+i).toString()+" adicionado"); //DEBUG USE
			}
			//dump newly createad hashmap to file
			storageFile.writeToFile(theaters);
			
		}
		*/
		
		//theaters = new ConcurrentHashMap<String, Theater>();
		//theatersBackup = new ConcurrentHashMap<String, Theater>(); //not used yet
		
	
		

		//7errors = 0;
		//System.out.println("tamanho do mapa é "+theaters.size());
	}

	private void checkDBfile() {
		// TODO Auto-generated method stub
		
	}

	//TESTING PROPOSES DELETE BEFORE EACH DELIVERIES
	public static void main(String[] args) throws IOException {
		DBServerImpl db = new DBServerImpl(1500, 0);
		
		
		
		
		
		
		
		//String teste[] = db.getTheaterNames();
		/*for (int i=0;i<theaters.size();i++)
			System.out.print(teste[i]+";");
		System.out.println("testing write file");
		*/
		//db.writeToFile();
		//System.out.println("done");
		//System.exit(0);
		
	}

	
	
	
	
	
	// RMI FUNCTIONS **********************************************************
	@Override
	public synchronized String[] getTheaterNames() throws RemoteException{
		//System.out.println("getTheaterNames");
		/* just in case a ordered array is needed
			String names[] = null;
			for (int i=0;i<theaters.size();i++)
				names[i]=theaters.get("TheaterNr"+i).theaterName;
			return names;
		*/
		Set<String> keys = theaters.keySet();
		String[] names = (String[]) keys.toArray(new String[keys.size()]);
		return names;
		
	}

	@Override
	public synchronized Theater getTheater(String theaterName) throws RemoteException{
		return theaters.get(theaterName);
	}

	@Override
	//ONLY CALL THIS FUCTION IF EXIST A PRIOR RESERVATION.
	//This validation should be done at appserver 
	public boolean occupySeat(String theaterName, Seat theaterSeat) throws RemoteException{
		//Theater theater = theaters.get(theaterName).seats
		if(theaters.get(theaterName).seats[theaterSeat.rowNr-'A'][theaterSeat.colNr].status==SeatStatus.FREE) {
			synchronized(this){
				theaters.get(theaterName).seats[theaterSeat.rowNr-'A'][theaterSeat.colNr].status=SeatStatus.OCCUPIED;
				if(mode>1)
					storageFile.buySeat(theaterName,theaterSeat);
			}
			return true;
		}
		else {
			errors++;
			System.out.println("OCCUPY ERROR [" + errors + "]: " + theaterName + " " + theaterSeat.getSeatName());
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

}