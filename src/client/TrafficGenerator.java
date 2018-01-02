package client;

import auxiliary.*;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;

/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class TrafficGenerator {

//	volatile static int requests;
//	volatile static int averageLatency;
//	volatile static int completeReqAverageLatency;
//	volatile static int cancelled;
//	volatile static int purchased;
//	volatile static int errors;
//	volatile static int latencyCounter = 0;
//	volatile static int completeRequestLatencyCounter = 0;

	// requests, purchased, cancelled, errors, average latency, complete request
	// average latency
	static int[] stats = new int[6];

	static final int NUM_SERVERS = 2;

	public static void main(String[] args) {

		try {
			String zkIP = "localhost";
			String zkPort = "";

			if (args.length > 0) {
				// args[0] = own IP
				System.setProperty("java.rmi.server.hostname", args[0]);

				// args[1] = zookeeper IP
				zkIP = args[1];
				// args[2] = zookeeper Port
				zkPort = args[2];
			}
			String zkAddress = zkIP + ":" + zkPort;
			final ConnectionHandler connector = new ConnectionHandler(zkAddress, null);
			final LoadBalancerIF loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer0", "/loadbalancer");

			ConfigHandler ch = new ConfigHandler();
			System.out.println(ch.toString());
			String origin = ch.getOrigin();
			String target = ch.getTarget();
			String targetTheater = ch.getTargetTheater();
			int numClients = ch.getNumClients();
			final int numTheaters = ch.getNumTheaters();
			String op = ch.getOp();
			int duration = ch.getDuration() * 1000; // milliseconds
			int rate = ch.getRate();
			int sleepRate = 1000 / rate;

			long endTime = System.currentTimeMillis() + duration;
			long startTime = System.currentTimeMillis();
			
			/*
			ConcurrentHashMap<String, Integer> stats = new ConcurrentHashMap<>(8);
			stats.put("requests", 0);
			stats.put("averageLatency", 0);
			stats.put("completeReqAverageLatency", 0);
			stats.put("cancelled", 0);
			stats.put("purchased", 0);
			stats.put("errors", 0);
			stats.put("latencyCounter", 0);
			stats.put("completeRequestLatencyCounter", 0);
			*/
//			int requests = 0;
//			int averageLatency = 0;
//			int completeReqAverageLatency = 0;
//			int cancelled = 0;
//			int purchased = 0;
//			int errors = 0;
//			int latencyCounter = 0;
//			int completeRequestLatencyCounter = 0;			
			
			

//			TrafficGeneratorThread r = new TrafficGeneratorThread(loadBalancerStub, numTheaters, requests,
//					averageLatency, completeReqAverageLatency, cancelled, errors, purchased, latencyCounter,
//					completeRequestLatencyCounter, origin, target, op, targetTheater, numClients, connector, NUM_SERVERS);

			TrafficGeneratorThread r = new TrafficGeneratorThread(loadBalancerStub, numTheaters, stats, origin, target, op, targetTheater, numClients, connector, NUM_SERVERS);			
			
			
/*
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					HashMap<String, String[]> theaters;
					long latencyBeg;
					long latencyEnd;
					long mainRequestLatency = 0;
					long latencydif;
					Random r = new Random();
					int aux = r.nextInt(numTheaters);
					String theaterName = "TheaterNr" + aux;
					try {
						latencyBeg = System.currentTimeMillis();
						theaters = loadBalancerStub.getNames();
						// check if loadbalancer is dead so we can connect to the backup
						latencyEnd = System.currentTimeMillis();
						latencyCounter++;
						latencydif = latencyEnd - latencyBeg;
						mainRequestLatency += latencydif;
						addToAverageLatency(latencydif);
						synchronized (requests) {
							stats[0]++;
						}

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
							synchronized (stats) {
								stats[0]++;
							}
						} catch (ConnectException | UnmarshalException e1) {
							System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary AppServer.");
							int appserverNr = Integer.parseInt(targetAppServer.substring(9));
							int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
							wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr, "/appserver");
							wideBoxStub = wideBoxStubBackup;
							System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);

							// m = wideBoxStub.query(theaterName);
						}

						latencyBeg = System.currentTimeMillis();
						m = wideBoxStub.query(theaterName);
						latencyEnd = System.currentTimeMillis();
						latencyCounter++;
						latencydif = latencyEnd - latencyBeg;
						mainRequestLatency += latencydif;
						addToAverageLatency(latencydif);
						synchronized (stats) {
							stats[0]++;
						}

						if (m.getType() == MessageType.AVAILABLE) {
							try {
								latencyBeg = System.currentTimeMillis();
								wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
								latencyEnd = System.currentTimeMillis();
								latencyCounter++;
								latencydif = latencyEnd - latencyBeg;
								mainRequestLatency += latencydif;
								completeRequestLatencyCounter++;
								addToCompleteRequestLatency(mainRequestLatency);
								addToAverageLatency(latencydif);
								synchronized (stats) {
									stats[0]++;
									stats[1]++;
								}
							} catch (ConnectException | UnmarshalException e1) {
								System.err.println("TRAFFICGEN ERROR RMI: Could not connect to primary DBServer.");
								int appserverNr = Integer.parseInt(targetAppServer.substring(9));
								int backupServerNr = Math.floorMod(appserverNr + 1, NUM_SERVERS);
								wideBoxStubBackup = (WideBoxIF) connector.get("appserver" + backupServerNr,
										"/appserver");
								wideBoxStub = wideBoxStubBackup;
								System.out.println("TRAFFICGEN : switched to backup APPSERVER" + backupServerNr);
								// ----------ASK-----------
								wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
							}

						} else {
							synchronized (stats) {
								stats[3]++;
							}
						}

					} catch (RemoteException e) {
						synchronized (stats) {
							stats[3]++;
						}
						e.printStackTrace();
					}
				}
			};
*/
			final ScheduledExecutorService ex = Executors.newScheduledThreadPool(100);

			Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
				@Override
				public void run() {
					ex.shutdown();
				}
			}, duration, TimeUnit.MILLISECONDS);
			ex.scheduleAtFixedRate(r/*timerTask*/, 0, sleepRate, TimeUnit.MILLISECONDS);
			while (!ex.isTerminated()) {
			}
			System.out.println("Num of requests made: " + stats[0] + "\n" + "Num of completed requests: " + stats[0] / 3
					+ "\n" + "Num of purchases made: " + stats[1] + "\n" + "Num of cancels made: " + stats[2] + "\n"
					+ "Num of errors gotten: " + stats[3] + "\n" + "Average latenty per request: " + stats[4] + "\n"
					+ "Average latency per completed request: " + stats[5] + "\n");
			
//			System.out.println("Num of requests made: " + requests + "\n" + "Num of completed requests: " + requests / 3
//					+ "\n" + "Num of purchases made: " + purchased + "\n" + "Num of cancels made: " + cancelled + "\n"
//					+ "Num of errors gotten: " + errors + "\n" + "Average latenty per request: " + averageLatency + "\n"
//					+ "Average latency per completed request: " + completeReqAverageLatency + "\n");

			System.out.println("Runtime (s): " + ((System.currentTimeMillis() - startTime) / 1000));
			System.exit(1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
/*
	private static synchronized void addToAverageLatency(long diff) {
		// System.out.print(diff+" ");
		stats[4] = Math.toIntExact(stats[4] + ((diff - stats[4]) / latencyCounter));
		// aux = Math.toIntExact(aux + ((diff - aux) / this.latencyCounter));
	}

	private static synchronized void addToCompleteRequestLatency(long diff) {
		stats[5] = Math.toIntExact(stats[5] + ((diff - stats[5]) / completeRequestLatencyCounter));
	}

	private static String getAppServerWithTheater(HashMap<String, String[]> theaters, int aux) {
		String targetTheater = "TheaterNr" + aux;
		for (Map.Entry<String, String[]> e : theaters.entrySet()) {
			for (String s : e.getValue()) {
				if (s.equals(targetTheater)) {
					return e.getKey();
				}
			}
		}
		return null;
	}*/

}
