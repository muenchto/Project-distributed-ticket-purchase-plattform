package dbserver;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
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
	final static String DELIMITER = ",";
	final static String LOGDELIMITER = "\n";
	//private static Integer num_theathers;
	private int firstTheater;
	private int lastTheater;
	//private boolean existDBFile=false; //flag to inform DBServer that exist or not a existant DBFile
	private static ConcurrentHashMap<String, Theater> theatersTemp = null ;


	
	public Storage(String dbInputFile, String logInputFile, int firstTheater, int lastTheater, int mode) throws IOException{
		//Storage.num_theathers=num_theathers;
		db = new File (dbInputFile);
		log = new File (logInputFile);
		this.firstTheater=firstTheater;
		this.lastTheater=lastTheater;
		this.mode = mode;
	
	}
	
	
	public synchronized void buySeat(String theaterName, Seat theaterSeat) {
		try {
			logfos =new FileOutputStream(log,true);
			logfosw = new OutputStreamWriter (logfos);
			logfd = logfos.getFD();
			//logfosw.append(theaterName+DELIMITER+theaterSeat.rowNr+DELIMITER+theaterSeat.colNr+DELIMITER+intValueSeat(theaterSeat)+"\n");
			logfosw.append(theaterName+LOGDELIMITER+theaterSeat.rowNr+LOGDELIMITER+theaterSeat.colNr+LOGDELIMITER);
			System.out.println("STORAGE:LOG> purchase made");
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
	

	public boolean existentDBfile() {
		return db.exists();
	}

	public boolean existentLOGfile() {
		return log.exists();
	}

	//write the all hashmap to DBFILENAME
	 public synchronized boolean saveToFile (ConcurrentHashMap<String, Theater> theaters) throws IOException {
		// Instant is for perfomance metrics (mesures the time it takes to dump the memory to file)
		Instant Start=null;
		Instant End=null;
		Start= Instant.now();
		//mode=1;
		//Initialization of the file to writing
		dbfos = new FileOutputStream(db); //this command is deleting the file FIX
		dbfosw = new OutputStreamWriter (dbfos);
		dbfd = dbfos.getFD();
		
		// create a set with the keys(theaters)
		//it's appearing without order
		//KeySetView<String, Theater> keys = theaters.keySet();
		//System.out.println(keys.toString());
		System.out.print("STORAGE: Starting dumpping DB to file....");

		/*for (int t=0;t<num_theathers;t++) {
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
		}*/
		//int t=0;
		for (Theater theater: theaters.values()) {
			dbfosw.write(theater.createStringForDB());
			//System.out.print(theater.createStringForDB()+"\n");
			/*
			 if(t%20==0)
			
				System.out.print(".");
			t++;
			*/
		}
		System.out.println("END");
		if (mode>=2) dbfd.sync();
		if (mode==3) dbfosw.flush();
		dbfosw.close();
		dbfos.close();
		End=Instant.now();
		System.out.println("STORAGE: Writing memory state to file took  "+Duration.between(Start,End).getSeconds()+" seconds or "+Duration.between(Start,End).getNano()/1000000+"M nanos");
		log.delete();
		System.out.println("STORAGE: Log file cleaned");
		return true;
	}
	

	public ConcurrentHashMap<String, Theater> loadDBfile() {
		theatersTemp = new ConcurrentHashMap<String, Theater> ();
		Instant Start=null;
		Instant End=null;
		Start = Instant.now();
		try {
			Scanner sc = new Scanner(db).useDelimiter("");
			String  theaterName;
			
			for (int t=firstTheater; t<lastTheater;t++) {
				theaterName = sc.nextLine();
				//System.out.println("reading theater -> "+ theaterName);
				theatersTemp.put(theaterName,  new  Theater(theaterName));
				for (int i = 0; i < 26; i++) {
					for (int j = 0; j < 40; j++) {
						if (sc.nextInt()==1){
							//theatersTemp.get(theaterName).seats[i][j].status=Seat.SeatStatus.values()[sc.nextInt()];
							theatersTemp.get(theaterName).occupySeat(i, j);
							//System.out.println(" reading from file at ["+i+"]["+j+"] the value ->"+ theatersTemp.get(theaterName).seats[i][j].status );
						}
					}
					sc.skip("\n");
				}
			}
			sc.close();
			System.out.println("Ending reading file");
		}
		catch (FileNotFoundException e) {
			System.err.println("STORAGE: File not found");
			return null;
		}//end catch

		End=Instant.now(); 
		System.out.println("File loaded in "+Duration.between(Start,End).getSeconds()+" seconds");
		
		//if a log file exists, then process log file
		if(existentLOGfile())
			return processLog(theatersTemp);
		else
			return new ConcurrentHashMap<String, Theater>(theatersTemp);
		
	}

	


	private ConcurrentHashMap<String, Theater> processLog(ConcurrentHashMap<String, Theater> theatersTemp) {
		String  theaterName = null;
		char row = 0;
		int col = 0;
		int op = 0;
		
		try {
			Scanner scLog = new Scanner(log).useDelimiter(LOGDELIMITER);
			while(scLog.hasNext()) {
				theaterName=scLog.next();
				row=(scLog.next()).charAt(0);
				col=scLog.nextInt();
				scLog.skip("\n");	
				theatersTemp.get(theaterName).occupySeat(row-'A', col);
				op++;
			}
			log.delete();
			System.out.println("STORAGE: Log file processed, with " + op + "operations pending and deleted");
		} catch (FileNotFoundException e) {
			System.err.println("File "+log.getName()+" dont exist");
			e.printStackTrace();
		}
		
		return new ConcurrentHashMap<String, Theater>(theatersTemp);
	}


}


