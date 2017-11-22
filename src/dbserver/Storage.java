package dbserver;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import auxiliary.Seat;
import auxiliary.Theater;
import auxiliary.Seat.SeatStatus;

public class Storage {
	
	public static FileInputStream dbfis = null;
	public static FileInputStream logfis = null;
	public static FileDescriptor dbfd = null;
	public static FileDescriptor logfd = null;
	private static OutputStreamWriter dbfosw;
	private static OutputStreamWriter logfosw; 
	private static FileOutputStream dbfos;
	private static FileOutputStream logfos;
	public static File db;
	public static File log;
	private static boolean SafeMode;
	private int mode;	
	final static String DELIMITER ="\n";
	private static Integer num_theathers=1500;
	private boolean existDBFile; //flag to inform DBServer that exist or not a existant DBFile
	private static ConcurrentHashMap<String, Theater> theatersTemp = null ;

	public Storage(String dbInputFile, String logInputFile, int num_theathers, int mode) throws IOException{
		Storage.num_theathers=num_theathers;
		db = new File (dbInputFile);
		log = new File (logInputFile);
		
		
		if(db.exists()) {
			System.out.println("DB file present");
			existDBFile=true;
			if (log.exists()) {
				System.out.println("log file present, initiate log processing");
				//processLog();
			}
			else
				try {
					log.createNewFile();
					System.out.println("Log file not present, a new log file was created");
				} catch (IOException e) {
					e.printStackTrace();
				}

		}
		else
			try {
				existDBFile=false;
				db.createNewFile();
				System.out.println("BD file not present, a new file was created");
			} catch (IOException e) {
				e.printStackTrace();
			}





		/*
		logfos =new FileOutputStream(logInputFile,true); //este comando esta a apagar o ficheiro
		logfosw = new OutputStreamWriter (logfos);
		logfd = logfos.getFD();
		*/
	}
	

	


	//TESTING PROPOSES DELETE BEFORE EACH DELIVERIES
	public static void main(String[] args) {
		if(db.exists())
			System.out.println("DB file present");
		else
			try {
				db.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


	}
	
	
	
	//write the all hashmap to DBFILENAME
	public boolean writeToFile (ConcurrentHashMap<String, Theater> theaters) throws IOException {
		// Instant is for perfomance metrics (mesures the time it takes to dump the memory to file)
		Instant Start=null;
		Instant End=null;
		Start= Instant.now();
		//mode=1;
		//Inicializacao do ficheiro para leitura
		dbfos = new FileOutputStream(db); //this command is deleting the file FIX
		dbfosw = new OutputStreamWriter (dbfos);
		dbfd = dbfos.getFD();
		
		// create a set with the keys(theaters)
		//it's appearing without order
		//KeySetView<String, Theater> keys = theaters.keySet();
		//System.out.println(keys.toString());
		System.out.println("STORAGE: Starting dumpping DB to file");
		for (int t=0;t<num_theathers;t++) {
			for (int i=0;i<26;i++){
				for(int j = 0; j < 40; j++) {
					try {				
						//Storing with teather as a int value, and the seat as a int value
						dbfosw.write(t+DELIMITER+i+DELIMITER+j+DELIMITER+intValueSeat(theaters.get("TheaterNr"+t).seats[i][j])+DELIMITER);
						//dbfosw.write(theaters.get("TheaterNr"+t).theaterName+DELIMITER+i+DELIMITER+j+DELIMITER+intValueSeat(theaters.get("TheaterNr"+t).seats[i][j])+DELIMITER);
						
						//Stores seat with a int value and theaters string value
						//System.out.println(theaters.get("TheaterNr"+t).theaterName+DELIMITER+i+DELIMITER+j+DELIMITER+intValueSeat(theaters.get("TheaterNr"+t).seats[i][j])+DELIMITER);
						
						//Stores seats with the string value and theaters string value
						//System.out.println(theaters.get("TheaterNr"+t).theaterName+DELIMITER+i+DELIMITER+j+DELIMITER+theaters.get("TheaterNr"+t).seats[i][j].status+DELIMITER);
					}

					catch (IOException e) {
						System.err.println("DBSERVERImpl - ERROR WRITING TO FILE");
						e.printStackTrace();
						return false;
					}//end catch

				}
			}
			if(t%20==0)
				System.out.print(".");
		}	
		System.out.println("END");
		if (mode>=2) dbfd.sync();
		if (mode==3) dbfosw.flush();
		dbfosw.close();
		dbfos.close();
		End=Instant.now();
		System.out.println("Writing memory state to file took  "+Duration.between(Start,End).getSeconds()+" seconds or "+Duration.between(Start,End).getNano()/1000000+"M nanos");
		return true;
	}
	
	public synchronized void buySeat(String theaterName, Seat theaterSeat) {
		try {
			logfosw.append(theaterName+DELIMITER+theaterSeat.rowNr+DELIMITER+theaterSeat.colNr+DELIMITER+intValueSeat(theaterSeat));
			System.out.println("LOG> purchase made");
			if (mode>=2) {
				logfosw.flush();
				//System.out.println("LOG> Flushing log");
			}			
			if (mode==3) {
				logfd.sync();
				//System.out.println("LOG> Syncronizing log");
			}
			logfosw.close();
		} catch (IOException e) {
			System.err.println("LOG> Error writing to log");
			//e.printStackTrace();
		}
	}
	
	//Auxiliary fuction to prevent writing the whole words to file
	int intValueSeat (Seat seat) {
		int resp=-1;
		if (seat.status==SeatStatus.FREE) resp=1;
		if (seat.status==SeatStatus.RESERVED) resp=2;
		if (seat.status==SeatStatus.OCCUPIED) resp=3;
		return resp;
	}


	public boolean existentDBfile() {
		return existDBFile;
	}


	public void saveToFile(ConcurrentHashMap<String, Theater> theaters) {
		// TODO Auto-generated method stub
		
	}


	public ConcurrentHashMap<String, Theater> loadDBfile() {
		theatersTemp = new ConcurrentHashMap<String, Theater> ();
		return null;
	}

	private void processLog() {
		// TODO Auto-generated method stub
		
	}


}


