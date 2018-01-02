package client;

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

	private int latencyCounter;
	private WideBoxIF wideBoxStub;
	private LoadBalancerIF loadBalancerStub;
	private ConnectionHandler connector;
	private int numTheaters;
	private int numRequests;
	private int numErrors;
	private int numPurchased;
	private long numAverageLatency;
	private int compReqAverageLatency;
	private int numCancelled;
	private String origin;
	private String target;
	private String op;
	private String targetTheater;
	private int NUM_SERVERS;
	private int compReqLatencyCounter;
	private int [] stats;
	

	public TrafficGeneratorThread(LoadBalancerIF loadBalancerStub, int numTheaters, int numRequests,
			long numAverageLatency, int compReqAverageLatency, int numCancelled, int numErros, int numPurchased,
			int latencyCounter, int compReqLatencyCounter, String origin, String target, String op,
			String targetTheater, int numClients, ConnectionHandler connector, int NUM_SERVERS) {
		this.wideBoxStub = null;
		this.loadBalancerStub = loadBalancerStub;
		this.numTheaters = numTheaters;
		this.numRequests = numRequests;
		this.numPurchased = numPurchased;
		this.numErrors = numErros;
		this.numAverageLatency = numAverageLatency;
		this.compReqAverageLatency = compReqAverageLatency;
		this.compReqLatencyCounter = compReqLatencyCounter;
		this.numCancelled = numCancelled;
		this.origin = origin;
		this.target = target;
		this.op = op;
		this.targetTheater = targetTheater;
		this.connector = connector;
		this.NUM_SERVERS = NUM_SERVERS;
	}

	public TrafficGeneratorThread(LoadBalancerIF loadBalancerStub, int numTheaters,
			int[] stats, String origin, String target, String op, String targetTheater,
			int numClients, ConnectionHandler connector, int NUM_SERVERS) {
		this.wideBoxStub = null;
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
		long endTime;
		Random r = new Random();
		int clientId = 1;
		if (origin.equals("single")) {
			if (target.equals("single")) {
				if (op.equals("query")) {
					//SSQRequest();
				} else { // op = purchase
					//SSPRequest();
				}
			} else { // target = random
				if (op.equals("query")) {
					//SRQRequest(r);
				} else { // op = purchase
					SRPRequest(r);
				}
			}
		} else { // origin = random
			if (target.equals("single")) {
				if (op.equals("query")) {
					//RSQRequest(clientId);
				} else { // op = purchase
					//RSPRequest(clientId);
				}
			} else { // target = random
				if (op.equals("query")) {
					//RRQRequest(r, clientId);
				} else { // op = purchase
					//RRPRequest(clientId, r);
				}
			}
		}
	}
/*
	public void SSQRequest() {
		String[] theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		try {
			latencyBeg = System.currentTimeMillis();
			theaters = wideBoxStub.getNames();
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			latencyBeg = System.currentTimeMillis();
			Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			if (m.getType() == MessageType.AVAILABLE) {
				latencyBeg = System.currentTimeMillis();
				wideBoxStub.cancel(theaters[Integer.parseInt(targetTheater)], m.getClientsSeat(), m.getClientID());
				latencyEnd = System.currentTimeMillis();
				this.latencyCounter++;
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				this.completeRequestLatencyCounter++;
				addToCompleteRequestLatency(mainRequestLatency);
				addToAverageLatency(latencydif);
				this.numCancelled[0]++;
				this.numCancelled[2]++;
			} else {
				this.numCancelled[3]++;
			}

			if (mainRequestLatency <= this.numAverageLatency) {
				this.numAverageLatency -= mainRequestLatency;
			}

			if (this.rateCounter == this.numRequests) {
				if (this.numAverageLatency > 0) {
					Thread.sleep(this.numAverageLatency);
				}
				this.numAverageLatency = 1000;
			}
			this.rateCounter++;

		} catch (RemoteException e) {
			this.numCancelled[3]++;
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void SSPRequest() {
		String[] theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		try {
			latencyBeg = System.currentTimeMillis();
			theaters = wideBoxStub.getNames();
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			latencyBeg = System.currentTimeMillis();
			Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			if (m.getType() == MessageType.AVAILABLE) {
				latencyBeg = System.currentTimeMillis();
				wideBoxStub.accept(theaters[Integer.parseInt(targetTheater)], m.getClientsSeat(), m.getClientID());
				latencyEnd = System.currentTimeMillis();
				this.latencyCounter++;
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				this.completeRequestLatencyCounter++;
				addToCompleteRequestLatency(mainRequestLatency);
				addToAverageLatency(latencydif);
				this.numCancelled[0]++;
				this.numCancelled[1]++;
			} else {
				this.numCancelled[3]++;
			}

			if (mainRequestLatency <= this.numAverageLatency) {
				this.numAverageLatency -= mainRequestLatency;
			}

			if (this.rateCounter == this.numRequests) {
				if (this.numAverageLatency > 0) {
					Thread.sleep(this.numAverageLatency);
				}
				this.numAverageLatency = 1000;
			}
			this.rateCounter++;

		} catch (RemoteException e) {
			this.numCancelled[3]++;
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void SRQRequest(Random r) {
		String[] theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		int aux = r.nextInt(this.numTheaters);
		try {
			latencyBeg = System.currentTimeMillis();
			theaters = wideBoxStub.getNames();
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			latencyBeg = System.currentTimeMillis();
			Message m = wideBoxStub.query(theaters[aux]);
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			if (m.getType() == MessageType.AVAILABLE) {
				latencyBeg = System.currentTimeMillis();
				wideBoxStub.cancel(theaters[aux], m.getClientsSeat(), m.getClientID());
				latencyEnd = System.currentTimeMillis();
				this.latencyCounter++;
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				this.completeRequestLatencyCounter++;
				addToCompleteRequestLatency(mainRequestLatency);
				addToAverageLatency(latencydif);
				this.numCancelled[0]++;
				this.numCancelled[2]++;
			} else {
				this.numCancelled[3]++;
			}

			if (mainRequestLatency <= this.numAverageLatency) {
				this.numAverageLatency -= mainRequestLatency;
			}

			if (this.rateCounter == this.numRequests) {
				if (this.numAverageLatency > 0) {
					Thread.sleep(this.numAverageLatency);
				}
				this.numAverageLatency = 1000;
			}
			this.rateCounter++;

		} catch (RemoteException e) {
			this.numCancelled[3]++;
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
*/
	public void SRPRequest(Random r) {
		HashMap<String, String[]> theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		int aux = r.nextInt(numTheaters);
		String theaterName = "TheaterNr" + aux;
		try {
			latencyBeg = System.currentTimeMillis();
			theaters = loadBalancerStub.getNames();
			// check if loadbalancer is dead so we can connect to the backup
			latencyEnd = System.currentTimeMillis();
			//this.latencyCounter++;
			this.stats.merge("requests", 0, (oldValue, one) -> oldValue + one);
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
//			synchronized (this.numRequests) {
//				this.numRequests++;
//			}
			this.stats.merge("requests", 0, (oldValue, one) -> oldValue + one);

			String targetAppServer = getAppServerWithTheater(theaters, aux);
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

			}

			Message m;
			try {

				latencyBeg = System.currentTimeMillis();
				m = wideBoxStub.query(theaterName);
				// check for null if the theater doesnt exist in the message m

				latencyEnd = System.currentTimeMillis();
				latencyCounter++;
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				addToAverageLatency(latencydif);
				//synchronized (stats) {
					this.numRequests++;
				//}
			} catch (ConnectException | UnmarshalException e1) {
				System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
				int appserverNr = Integer.parseInt(targetAppServer.substring(9));
				int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
				wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
				wideBoxStub = wideBoxStubBackup;
				System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
			}

			latencyBeg = System.currentTimeMillis();
			m = wideBoxStub.query(theaterName);
			latencyEnd = System.currentTimeMillis();
			latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			//synchronized (stats) {
			this.numRequests++;
			//}

			if (m.getType() == MessageType.AVAILABLE) {
				try {
					latencyBeg = System.currentTimeMillis();
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					latencyEnd = System.currentTimeMillis();
					latencyCounter++;
					latencydif = latencyEnd - latencyBeg;
					mainRequestLatency += latencydif;
					compReqLatencyCounter++;
					addToCompleteRequestLatency(mainRequestLatency);
					addToAverageLatency(latencydif);
					this.numRequests++;
					this.numPurchased++;				
//					synchronized (stats) {
//						stats[0]++;
//						stats[1]++;
//					}
				} catch (ConnectException | UnmarshalException e1) {
					/*
					System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
					int appserverNr = Integer.parseInt(targetAppServer.substring(9));
					int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
					wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
							"/appserver");
					wideBoxStub = wideBoxStubBackup;
					System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
					// ----------ASK-----------
					wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
					*/
					this.numErrors++;
				}

			} else {
				this.numErrors++;
			}

		} catch (RemoteException e) {
			this.numErrors++;
			e.printStackTrace();
		}
	}
/*
	public void RSQRequest(int clientId) {
		String[] theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		try {
			latencyBeg = System.currentTimeMillis();
			theaters = wideBoxStub.getNames();
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			latencyBeg = System.currentTimeMillis();
			Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			if (m.getType() == MessageType.AVAILABLE) {
				latencyBeg = System.currentTimeMillis();
				wideBoxStub.cancel(theaters[Integer.parseInt(targetTheater)], m.getClientsSeat(), m.getClientID());
				latencyEnd = System.currentTimeMillis();
				this.latencyCounter++;
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				this.completeRequestLatencyCounter++;
				addToCompleteRequestLatency(mainRequestLatency);
				addToAverageLatency(latencydif);
				this.numCancelled[0]++;
				this.numCancelled[2]++;
			} else {
				this.numCancelled[3]++;
			}

			if (mainRequestLatency <= this.numAverageLatency) {
				this.numAverageLatency -= mainRequestLatency;
			}

			if (this.rateCounter == this.numRequests) {
				if (this.numAverageLatency > 0) {
					Thread.sleep(this.numAverageLatency);
				}
				this.numAverageLatency = 1000;
			}
			this.rateCounter++;

		} catch (RemoteException e) {
			this.numCancelled[3]++;
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void RSPRequest(int clientId) {
		String[] theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		try {
			latencyBeg = System.currentTimeMillis();
			theaters = wideBoxStub.getNames();
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			latencyBeg = System.currentTimeMillis();
			Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			if (m.getType() == MessageType.AVAILABLE) {
				latencyBeg = System.currentTimeMillis();
				wideBoxStub.accept(theaters[Integer.parseInt(targetTheater)], m.getClientsSeat(), m.getClientID());
				latencyEnd = System.currentTimeMillis();
				this.latencyCounter++;
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				this.completeRequestLatencyCounter++;
				addToCompleteRequestLatency(mainRequestLatency);
				addToAverageLatency(latencydif);
				this.numCancelled[0]++;
				this.numCancelled[1]++;
			} else {
				this.numCancelled[3]++;
			}

			if (mainRequestLatency <= this.numAverageLatency) {
				this.numAverageLatency -= mainRequestLatency;
			}

			if (this.rateCounter == this.numRequests) {
				if (this.numAverageLatency > 0) {
					Thread.sleep(this.numAverageLatency);
				}
				this.numAverageLatency = 1000;
			}
			this.rateCounter++;

		} catch (RemoteException e) {
			this.numCancelled[3]++;
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void RRQRequest(Random r, int clientId) {
		String[] theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		int aux = r.nextInt(this.numTheaters);
		try {
			latencyBeg = System.currentTimeMillis();
			theaters = wideBoxStub.getNames();
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			latencyBeg = System.currentTimeMillis();
			Message m = wideBoxStub.query(theaters[aux]);
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			if (m.getType() == MessageType.AVAILABLE) {
				latencyBeg = System.currentTimeMillis();
				wideBoxStub.cancel(theaters[aux], m.getClientsSeat(), m.getClientID());
				latencyEnd = System.currentTimeMillis();
				this.latencyCounter++;
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				this.completeRequestLatencyCounter++;
				addToCompleteRequestLatency(mainRequestLatency);
				addToAverageLatency(latencydif);
				this.numCancelled[0]++;
				this.numCancelled[2]++;
			} else {
				this.numCancelled[3]++;
			}

			if (mainRequestLatency <= this.numAverageLatency) {
				this.numAverageLatency -= mainRequestLatency;
			}

			if (this.rateCounter == this.numRequests) {
				if (this.numAverageLatency > 0) {
					Thread.sleep(this.numAverageLatency);
				}
				this.numAverageLatency = 1000;
			}
			this.rateCounter++;

		} catch (RemoteException e) {
			this.numCancelled[3]++;
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void RRPRequest(int clientId, Random r) {
		String[] theaters;
		long latencyBeg;
		long latencyEnd;
		long mainRequestLatency = 0;
		long latencydif;
		int aux = r.nextInt(this.numTheaters);
		try {
			latencyBeg = System.currentTimeMillis();
			theaters = wideBoxStub.getNames();
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			latencyBeg = System.currentTimeMillis();
			Message m = wideBoxStub.query(theaters[aux]);
			latencyEnd = System.currentTimeMillis();
			this.latencyCounter++;
			latencydif = latencyEnd - latencyBeg;
			mainRequestLatency += latencydif;
			addToAverageLatency(latencydif);
			this.numCancelled[0]++;

			if (m.getType() == MessageType.AVAILABLE) {
				latencyBeg = System.currentTimeMillis();
				wideBoxStub.accept(theaters[aux], m.getClientsSeat(), m.getClientID());
				latencyEnd = System.currentTimeMillis();
				this.latencyCounter++;
				latencydif = latencyEnd - latencyBeg;
				mainRequestLatency += latencydif;
				this.completeRequestLatencyCounter++;
				addToCompleteRequestLatency(mainRequestLatency);
				addToAverageLatency(latencydif);
				this.numCancelled[0]++;
				this.numCancelled[1]++;
			} else {
				this.numCancelled[3]++;
			}

			if (mainRequestLatency <= this.numAverageLatency) {
				this.numAverageLatency -= mainRequestLatency;
			}

			if (this.rateCounter == this.numRequests) {
				if (this.numAverageLatency > 0) {
					Thread.sleep(this.numAverageLatency);
				}
				this.numAverageLatency = 1000;
			}
			this.rateCounter++;

		} catch (RemoteException e) {
			this.numCancelled[3]++;
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
*/
	private synchronized void addToAverageLatency(long diff) {
		this.stats[4] = Math
				.toIntExact(this.stats[4] + ((diff - this.stats[4]) / this.stats[4]));
	}

	private synchronized void addToCompleteRequestLatency(long diff) {
		this.stats[5] = Math.toIntExact(
				this.stats[5] + ((diff - this.stats[5]) / this.stats[5]));
	}

	private String getAppServerWithTheater(HashMap<String, String[]> theaters, int aux) {
		String targetTheater = "TheaterNr" + aux;
		for (Entry<String, String[]> e : theaters.entrySet()) {
			for (String s : e.getValue()) {
				if (s.equals(targetTheater)) {
					return e.getKey();
				}
			}
		}
		return null;
	}
/*
	@Override
	public String toString() {
		return "Num of requests made: " + this.numCancelled[0] + "\n" + "Num of completed requests: "
				+ this.numCancelled[0] / 3 + "\n" + "Num of purchases made: " + this.numCancelled[1] + "\n"
				+ "Num of cancels made: " + this.numCancelled[2] + "\n" + "Num of errors gotten: "
				+ this.numCancelled[3] + "\n" + "Average latenty per request: " + this.numCancelled[4] + "\n"
				+ "Average latency per completed request: " + this.numCancelled[5] + "\n";
	}*/
}
