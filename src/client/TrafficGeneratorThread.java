package client;

import appserver.LoadBalancer;
import auxiliary.ConnectionHandler;
import auxiliary.LoadBalancerIF;
import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.WideBoxIF;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PSD Project
 * 
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class TrafficGeneratorThread implements Runnable {
	
	private LoadBalancerIF loadBalancerStub;
	private ConnectionHandler connector;
	private int numTheaters;
	private String origin;
	private String target;
	private String op;
	private String targetTheater;
	private int NUM_SERVERS;
	
	private int [] stats;
	

//	public TrafficGeneratorThread(LoadBalancerIF loadBalancerStub, int numTheaters, Integer numRequests,
//			Integer numAverageLatency, Integer compReqAverageLatency, Integer numCancelled, Integer numErros, Integer numPurchased,
//			Integer latencyCounter, Integer compReqLatencyCounter, String origin, String target, String op,
//			String targetTheater, int numClients, ConnectionHandler connector, int NUM_SERVERS) {
//		this.wideBoxStub = null;
//		this.loadBalancerStub = loadBalancerStub;
//		this.numTheaters = numTheaters;
//		this.numRequests = numRequests;
//		this.numPurchased = numPurchased;
//		this.numErrors = numErros;
//		this.numAverageLatency = numAverageLatency;
//		this.compReqAverageLatency = compReqAverageLatency;
//		this.compReqLatencyCounter = compReqLatencyCounter;
//		this.numCancelled = numCancelled;
//		this.origin = origin;
//		this.target = target;
//		this.op = op;
//		this.targetTheater = targetTheater;
//		this.connector = connector;
//		this.NUM_SERVERS = NUM_SERVERS;
//	}

	public TrafficGeneratorThread(LoadBalancerIF loadBalancerStub, int numTheaters,
			int[] stats, String origin, String target, String op, String targetTheater,
			int numClients, ConnectionHandler connector, int NUM_SERVERS) {
		this.loadBalancerStub = loadBalancerStub;
		this.numTheaters = numTheaters;
		this.origin = origin;
		this.target = target;
		this.op = op;
		this.targetTheater = targetTheater;
		this.connector = connector;
		this.NUM_SERVERS = NUM_SERVERS;
		this.stats = stats;
	}

	@Override
	public void run() {

		//System.out.println(Thread.currentThread().getName());
		Random r = new Random();
		int clientId = 1;
		if (origin.equals("single")) {
			if (target.equals("single")) {
				if (op.equals("query")) {
					SSQRequest();
				} else { // op = purchase
					SSPRequest();
				}
			} else { // target = random
				if (op.equals("query")) {
					SRQRequest(r);
				} else { // op = purchase
					SRPRequest(r);
				}
			}
		} else { // origin = random
			if (target.equals("single")) {
				if (op.equals("query")) {
					RSQRequest(clientId);
				} else { // op = purchase
					RSPRequest(clientId);
				}
			} else { // target = random
				if (op.equals("query")) {
					RRQRequest(r, clientId);
				} else { // op = purchase
					RRPRequest(clientId, r);
				}
			}
		}
	}

	public void SSQRequest() {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		String theaterName = "TheaterNr" + targetTheater;
		try {
			try {
				latencyBeg = System.currentTimeMillis();
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				latencyBeg = System.currentTimeMillis();
				loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer1", "/loadbalancer");
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
				System.out.println("TRAFFICGEN : switched to backup LOADBALANCER1");
			}

			synchronized (this.stats) {
				this.stats[6]++;	
			}
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			synchronized (this.stats) {
				this.stats[0]++;
			}

			String targetAppServer = getAppServerWithTheater(theaters, theaterName);
			WideBoxIF wideBoxStub;
			WideBoxIF wideBoxStubBackup;
			try {
				wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
			} catch (ConnectException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);

			}

			Message m = null;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				latencyEnd = System.currentTimeMillis();
				synchronized (this.stats) {
					this.stats[6]++;	
				}
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				synchronized (this.stats) {
					this.stats[0]++;	
				}
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}
			
			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.cancel(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					synchronized (this.stats) {
						this.stats[6]++;	
					}
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					synchronized (this.stats) {
						this.stats[7]++;	
					}
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					synchronized (this.stats) {
						this.stats[0]++;
						this.stats[2]++;
						
					}
				} catch (ConnectException | UnmarshalException e1) {
					
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					/*int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					synchronized (this.stats) {
						this.stats[3]++;
					}
					
				}

			} else {
				synchronized (this.stats) {
					this.stats[3]++;
				}			
			}

		} catch (RemoteException e) {
			synchronized (this.stats) {
				this.stats[3]++;
			}
			e.printStackTrace();
		}
	}

	public void SSPRequest() {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		String theaterName = "TheaterNr" + targetTheater;
		try {
			try {
				latencyBeg = System.currentTimeMillis();
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				latencyBeg = System.currentTimeMillis();
				loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer1", "/loadbalancer");
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
				System.out.println("TRAFFICGEN : switched to backup LOADBALANCER1");
			}
			synchronized (this.stats) {
				this.stats[6]++;	
			}
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			synchronized (this.stats) {
				this.stats[0]++;
			}

			String targetAppServer = getAppServerWithTheater(theaters, theaterName);
			WideBoxIF wideBoxStub;
			WideBoxIF wideBoxStubBackup;
			try {
				wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
			} catch (ConnectException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);

			}

			Message m = null;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				latencyEnd = System.currentTimeMillis();
				synchronized (this.stats) {
					this.stats[6]++;	
				}
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				synchronized (this.stats) {
					this.stats[0]++;	
				}
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}
			
			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					synchronized (this.stats) {
						this.stats[6]++;	
					}
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					synchronized (this.stats) {
						this.stats[7]++;	
					}
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					synchronized (this.stats) {
						this.stats[0]++;
						this.stats[1]++;
						
					}
				} catch (ConnectException | UnmarshalException e1) {
					
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					/*int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					synchronized (this.stats) {
						this.stats[3]++;
					}
					
				}

			} else {
				synchronized (this.stats) {
					this.stats[3]++;
				}			
			}

		} catch (RemoteException e) {
			synchronized (this.stats) {
				this.stats[3]++;
			}
			e.printStackTrace();
		}
	}

	public void SRQRequest(Random r) {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		int aux = r.nextInt(numTheaters);
		String theaterName = "TheaterNr" + aux;
		try {
			try {
				latencyBeg = System.currentTimeMillis();
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				latencyBeg = System.currentTimeMillis();
				loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer1", "/loadbalancer");
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
				System.out.println("TRAFFICGEN : switched to backup LOADBALANCER1");
			}
			synchronized (this.stats) {
				this.stats[6]++;	
			}
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			synchronized (this.stats) {
				this.stats[0]++;
			}

			String targetAppServer = getAppServerWithTheater(theaters, theaterName);
			WideBoxIF wideBoxStub;
			WideBoxIF wideBoxStubBackup;
			try {
				wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
			} catch (ConnectException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);

			}

			Message m = null;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				latencyEnd = System.currentTimeMillis();
				synchronized (this.stats) {
					this.stats[6]++;	
				}
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				synchronized (this.stats) {
					this.stats[0]++;	
				}
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}
			

			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.cancel(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					synchronized (this.stats) {
						this.stats[6]++;	
					}
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					synchronized (this.stats) {
						this.stats[7]++;	
					}
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					synchronized (this.stats) {
						this.stats[0]++;
						this.stats[2]++;
						
					}
				} catch (ConnectException | UnmarshalException e1) {
					
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					/*int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					synchronized (this.stats) {
						this.stats[3]++;
					}
					
				}

			} else {
				synchronized (this.stats) {
					this.stats[3]++;
				}			
			}

		} catch (RemoteException e) {
			synchronized (this.stats) {
				this.stats[3]++;
			}
			e.printStackTrace();
		}
	}

	public void SRPRequest(Random r) {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		int aux = r.nextInt(numTheaters);
		String theaterName = "TheaterNr" + aux;
		try {

			try {
				latencyBeg = System.currentTimeMillis();
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				latencyBeg = System.currentTimeMillis();
				loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer1", "/loadbalancer");
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
				System.out.println("TRAFFICGEN : switched to backup LOADBALANCER1");
			}
			synchronized (this.stats) {
				this.stats[6]++;	
			}
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			synchronized (this.stats) {
				this.stats[0]++;
			}

			String targetAppServer = getAppServerWithTheater(theaters, theaterName);
			WideBoxIF wideBoxStub;
			WideBoxIF wideBoxStubBackup;
			try {
				wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
			} catch (ConnectException e1) {

				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
				synchronized (this.stats) {
					this.stats[3]++;
				}
			}

			Message m = null;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				latencyEnd = System.currentTimeMillis();
				synchronized (this.stats) {
					this.stats[6]++;	
				}
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				synchronized (this.stats) {
					this.stats[0]++;	
				}
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}
			

			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					synchronized (this.stats) {
						this.stats[6]++;	
					}
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					synchronized (this.stats) {
						this.stats[7]++;	
					}
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					synchronized (this.stats) {
						this.stats[0]++;
						this.stats[1]++;
						
					}
				} catch (ConnectException | UnmarshalException e1) {
					
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					/*int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					synchronized (this.stats) {
						this.stats[3]++;
					}
					
				}

			} else {
				synchronized (this.stats) {
					this.stats[3]++;
				}			
			}

		} catch (RemoteException e) {
			synchronized (this.stats) {
				this.stats[3]++;
			}
			e.printStackTrace();
		}
	}

	public void RSQRequest(int clientId) {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		String theaterName = "TheaterNr" + targetTheater;
		try {
			try {
				latencyBeg = System.currentTimeMillis();
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				latencyBeg = System.currentTimeMillis();
				loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer1", "/loadbalancer");
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
				System.out.println("TRAFFICGEN : switched to backup LOADBALANCER1");
			}
			synchronized (this.stats) {
				this.stats[6]++;	
			}
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			synchronized (this.stats) {
				this.stats[0]++;
			}

			String targetAppServer = getAppServerWithTheater(theaters, theaterName);
			WideBoxIF wideBoxStub;
			WideBoxIF wideBoxStubBackup;
			try {
				wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
			} catch (ConnectException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);

			}

			Message m = null;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				latencyEnd = System.currentTimeMillis();
				synchronized (this.stats) {
					this.stats[6]++;	
				}
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				synchronized (this.stats) {
					this.stats[0]++;	
				}
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}
			
			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.cancel(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					synchronized (this.stats) {
						this.stats[6]++;	
					}
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					synchronized (this.stats) {
						this.stats[7]++;	
					}
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					synchronized (this.stats) {
						this.stats[0]++;
						this.stats[2]++;
						
					}
				} catch (ConnectException | UnmarshalException e1) {
					
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					/*int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					synchronized (this.stats) {
						this.stats[3]++;
					}
					
				}

			} else {
				synchronized (this.stats) {
					this.stats[3]++;
				}			
			}

		} catch (RemoteException e) {
			synchronized (this.stats) {
				this.stats[3]++;
			}
			e.printStackTrace();
		}
	}

	public void RSPRequest(int clientId) {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		String theaterName = "TheaterNr" + targetTheater;
		try {
			latencyBeg = System.currentTimeMillis();
			try {
				latencyBeg = System.currentTimeMillis();
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				latencyBeg = System.currentTimeMillis();
				loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer1", "/loadbalancer");
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
				System.out.println("TRAFFICGEN : switched to backup LOADBALANCER1");
			}
			latencyEnd = System.currentTimeMillis();
			synchronized (this.stats) {
				this.stats[6]++;	
			}
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			synchronized (this.stats) {
				this.stats[0]++;
			}

			String targetAppServer = getAppServerWithTheater(theaters, theaterName);
			WideBoxIF wideBoxStub;
			WideBoxIF wideBoxStubBackup;
			try {
				wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
			} catch (ConnectException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);

			}

			Message m = null;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				latencyEnd = System.currentTimeMillis();
				synchronized (this.stats) {
					this.stats[6]++;	
				}
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				synchronized (this.stats) {
					this.stats[0]++;	
				}
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}
			
			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					synchronized (this.stats) {
						this.stats[6]++;	
					}
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					synchronized (this.stats) {
						this.stats[7]++;	
					}
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					synchronized (this.stats) {
						this.stats[0]++;
						this.stats[1]++;
						
					}
				} catch (ConnectException | UnmarshalException e1) {
					
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					/*int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					synchronized (this.stats) {
						this.stats[3]++;
					}
					
				}

			} else {
				synchronized (this.stats) {
					this.stats[3]++;
				}			
			}

		} catch (Exception e) {
			synchronized (this.stats) {
				this.stats[3]++;
			}
			e.printStackTrace();
		}
	}

	public void RRQRequest(Random r, int clientId) {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		int aux = r.nextInt(numTheaters);
		String theaterName = "TheaterNr" + aux;
		try {
			latencyBeg = System.currentTimeMillis();
			try {
				latencyBeg = System.currentTimeMillis();
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				latencyBeg = System.currentTimeMillis();
				loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer1", "/loadbalancer");
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
				System.out.println("TRAFFICGEN : switched to backup LOADBALANCER1");
			}
			latencyEnd = System.currentTimeMillis();
			synchronized (this.stats) {
				this.stats[6]++;	
			}
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			synchronized (this.stats) {
				this.stats[0]++;
			}

			String targetAppServer = getAppServerWithTheater(theaters, theaterName);
			WideBoxIF wideBoxStub;
			WideBoxIF wideBoxStubBackup;
			try {
				wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
			} catch (ConnectException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);

			}

			Message m = null;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				latencyEnd = System.currentTimeMillis();
				synchronized (this.stats) {
					this.stats[6]++;	
				}
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				synchronized (this.stats) {
					this.stats[0]++;	
				}
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}
			

			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.cancel(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					synchronized (this.stats) {
						this.stats[6]++;	
					}
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					synchronized (this.stats) {
						this.stats[7]++;	
					}
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					synchronized (this.stats) {
						this.stats[0]++;
						this.stats[2]++;
						
					}
				} catch (ConnectException | UnmarshalException e1) {
					
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					/*int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					synchronized (this.stats) {
						this.stats[3]++;
					}
					
				}

			} else {
				synchronized (this.stats) {
					this.stats[3]++;
				}			
			}

		} catch (RemoteException e) {
			synchronized (this.stats) {
				this.stats[3]++;
			}
			e.printStackTrace();
		}
	}

	public void RRPRequest(int clientId, Random r) {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		int aux = r.nextInt(numTheaters);
		String theaterName = "TheaterNr" + aux;
		try {
			latencyBeg = System.currentTimeMillis();
			try {
				latencyBeg = System.currentTimeMillis();
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				latencyBeg = System.currentTimeMillis();
				loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer1", "/loadbalancer");
				theaters = loadBalancerStub.getNames();
				latencyEnd = System.currentTimeMillis();
				System.out.println("TRAFFICGEN : switched to backup LOADBALANCER1");
			}
			latencyEnd = System.currentTimeMillis();
			synchronized (this.stats) {
				this.stats[6]++;	
			}
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			synchronized (this.stats) {
				this.stats[0]++;
			}

			String targetAppServer = getAppServerWithTheater(theaters, theaterName);
			WideBoxIF wideBoxStub;
			WideBoxIF wideBoxStubBackup;
			try {
				wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
			} catch (ConnectException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);

			}

			Message m = null;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				latencyEnd = System.currentTimeMillis();
				synchronized (this.stats) {
					this.stats[6]++;	
				}
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				synchronized (this.stats) {
					this.stats[0]++;	
				}
			} catch (ConnectException | UnmarshalException e1) {
				synchronized (this.stats) {
					this.stats[3]++;
				}
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}
			

			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					synchronized (this.stats) {
						this.stats[6]++;	
					}
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					synchronized (this.stats) {
						this.stats[7]++;	
					}
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					synchronized (this.stats) {
						this.stats[0]++;
						this.stats[1]++;
						
					}
				} catch (ConnectException | UnmarshalException e1) {
					
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					/*int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					synchronized (this.stats) {
						this.stats[3]++;
					}
					
				}

			} else {
				synchronized (this.stats) {
					this.stats[3]++;
				}			
			}

		} catch (RemoteException e) {
			synchronized (this.stats) {
				this.stats[3]++;
			}
			e.printStackTrace();
		}
	}

	private synchronized void addToAverageLatency(long diff) {
		this.stats[4] = Math.toIntExact(this.stats[4] + ((diff - this.stats[4]) / this.stats[6]));
	}

	private synchronized void addToCompleteRequestLatency(long diff) {
		this.stats[5] = Math.toIntExact(this.stats[5] + ((diff - this.stats[5]) / this.stats[7]));
	}

	private String getAppServerWithTheater(HashMap<String, String[]> theaters, String theaterName) {
		for (Entry<String, String[]> e : theaters.entrySet()) {
			for (String s : e.getValue()) {
				if (s.equals(theaterName)) {
					return e.getKey();
				}
			}
		}
		return null;
	}
}
