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

public class Storage {

	public FileInputStream dbfis = null;
	public FileInputStream logfis = null;
	public FileInputStream dbfisback = null;
	public FileInputStream logfisback = null;

	public FileDescriptor dbfd = null;
	public FileDescriptor logfd = null;
	public FileDescriptor dbfdback = null;
	public FileDescriptor logfdback = null;

	private OutputStreamWriter dbfosw;
	private OutputStreamWriter logfosw;
	private OutputStreamWriter dbfoswback;
	private OutputStreamWriter logfoswback;

	private FileOutputStream dbfos;
	private FileOutputStream logfos;
	private FileOutputStream dbfosback;
	private FileOutputStream logfosback;

	String DBFILENAME;
	String LOGFILENAME;
	String DBFILENAMEBACKUP;
	String LOGFILENAMEBACKUP;

	public File db;
	public File log;
	public File dbback;
	public File logback;
	private  boolean SafeMode;
	private int mode;
	final  String DELIMITER = ",";
	final  String LOGDELIMITER = "\n";
	//private static Integer num_theathers;
	private int firstTheater;
	private int lastTheater;
	private int firstBackTheater;
	//private boolean existDBFile=false; //flag to inform DBServer that exist or not a existant DBFile
	private ConcurrentHashMap<String, Theater> theatersTemp = null ;

	//old one
	public Storage(int index, int firstTheater, int lastTheater, int firstBackTheater, int mode) throws IOException{
		this.firstTheater=firstTheater;
		this.lastTheater=lastTheater;
		this.firstBackTheater = firstBackTheater;

		this.mode = mode;

		DBFILENAME = index+"DBfile.txt";
		LOGFILENAME = index+"LOGfile.txt";
		DBFILENAMEBACKUP = index+"DB_BACKUPfile.txt";
		LOGFILENAMEBACKUP = index+"LOG_BACKUPfile.txt";
		db = new File (DBFILENAME);
		log = new File (LOGFILENAME);
		dbback = new File (DBFILENAMEBACKUP);
		logback = new File (LOGFILENAMEBACKUP);
	}


	public Storage (int index, int totalServers) throws IOException {


		this.mode = mode;
	}




	public synchronized void buySeat(String theaterName, Seat theaterSeat) {
		try {
			logfos =new FileOutputStream(log,true);
			logfosw = new OutputStreamWriter (logfos);
			logfd = logfos.getFD();
			//logfosw.append(theaterName+DELIMITER+theaterSeat.rowNr+DELIMITER+theaterSeat.colNr+DELIMITER+intValueSeat(theaterSeat)+"\n");
			logfosw.append(theaterName+LOGDELIMITER+theaterSeat.rowNr+LOGDELIMITER+theaterSeat.colNr+LOGDELIMITER);
			//System.out.println("STORAGE:LOG> purchase made");
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

	public synchronized void buySeatInBackup(String theaterName, Seat theaterSeat) {
		try {
			logfosback =new FileOutputStream(logback,true);
			logfoswback = new OutputStreamWriter (logfosback);
			logfdback = logfosback.getFD();
			//logfosw.append(theaterName+DELIMITER+theaterSeat.rowNr+DELIMITER+theaterSeat.colNr+DELIMITER+intValueSeat(theaterSeat)+"\n");
			logfoswback.append(theaterName+LOGDELIMITER+theaterSeat.rowNr+LOGDELIMITER+theaterSeat.colNr+LOGDELIMITER);
			//System.out.println("STORAGE:LOG> purchase made");
			if (mode>=2) {
				logfoswback.flush();
				//System.out.println("LOG> Flushing log");
			}
			if (mode==3) {
				logfdback.sync();
				//System.out.println("LOG> Syncronizing log");
			}
			logfoswback.close();
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

	public boolean existentDBbackfile() {
		return dbback.exists();
	}

	public boolean existentLOGbackfile() {
		return logback.exists();
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
		System.out.print("STORAGE: Starting dumpping DB to file....");
		for (Theater theater: theaters.values()) {
			dbfosw.write(theater.createStringForDB());
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

	//write the all hashmap to DBFILENAMEBACKUP
	public synchronized boolean saveToBackFile (ConcurrentHashMap<String, Theater> theaters) throws IOException {
		// Instant is for perfomance metrics (mesures the time it takes to dump the memory to file)
		Instant Start=null;
		Instant End=null;
		Start= Instant.now();
		//mode=1;
		//Initialization of the file to writing
		dbfosback = new FileOutputStream(dbback); //this command is deleting the file FIX
		dbfoswback = new OutputStreamWriter (dbfosback);
		dbfdback = dbfosback.getFD();
		// create a set with the keys(theaters)
		System.out.print("STORAGE: Starting dumpping DB backup to file....");
		for (Theater theater: theaters.values()) {
			dbfoswback.write(theater.createStringForDB());
		}
		System.out.println("END");
		if (mode>=2) dbfdback.sync();
		if (mode==3) dbfoswback.flush();
		dbfoswback.close();
		dbfosback.close();
		End=Instant.now();
		System.out.println("STORAGE: Writing memory state to file took  "+Duration.between(Start,End).getSeconds()+" seconds or "+Duration.between(Start,End).getNano()/1000000+"M nanos");
		logback.delete();
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

	public ConcurrentHashMap<String, Theater> loadDBBackfile() {
		theatersTemp = new ConcurrentHashMap<String, Theater> ();
		Instant Start=null;
		Instant End=null;
		Start = Instant.now();
		try {
			Scanner sc = new Scanner(dbback).useDelimiter("");
			String  theaterName;
			for (int t=firstBackTheater; t<firstTheater;t++) {
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
		if(existentLOGbackfile())
			return processbackLog(theatersTemp);
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
			System.out.println("STORAGE: Log file processed, with " + op + " operations pending and deleted");
		} catch (FileNotFoundException e) {
			System.err.println("File "+log.getName()+" dont exist");
			e.printStackTrace();
		}
		return new ConcurrentHashMap<String, Theater>(theatersTemp);
	}

	private ConcurrentHashMap<String, Theater> processbackLog(ConcurrentHashMap<String, Theater> theatersTemp) {
		String  theaterName = null;
		char row = 0;
		int col = 0;
		int op = 0;
		try {
			Scanner scLog = new Scanner(logback).useDelimiter(LOGDELIMITER);
			while(scLog.hasNext()) {
				theaterName=scLog.next();
				row=(scLog.next()).charAt(0);
				col=scLog.nextInt();
				scLog.skip("\n");
				if(theatersTemp.containsKey(theaterName))
					theatersTemp.get(theaterName).occupySeat(row-'A', col);
				else
					System.out.println("Cannot find "+theaterName+" in the hashmap that was loaded by from "+DBFILENAMEBACKUP);
				op++;
			}
			logback.delete();
			System.out.println("STORAGE: Log file processed, with " + op + " operations pending and deleted");
		} catch (FileNotFoundException e) {
			System.err.println("File "+logback.getName()+" dont exist");
			e.printStackTrace();
		}
		return new ConcurrentHashMap<String, Theater>(theatersTemp);
	}

}




