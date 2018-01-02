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

//	volatile static Integer requests;
//	volatile static Integer averageLatency;
//	volatile static Integer completeReqAverageLatency;
//	volatile static Integer cancelled;
//	volatile static Integer purchased;
//	volatile static Integer errors;
//	volatile static Integer latencyCounter = 0;
//	volatile static Integer completeRequestLatencyCounter = 0;

	// requests, purchased, cancelled, errors, average latency, complete request
	// average latency, average latency counter, comp req latency counter
	static int[] stats = new int[8];

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
//			requests = new Integer(0);
//			averageLatency = new Integer(0);
//			completeReqAverageLatency = new Integer(0);
//			cancelled = new Integer(0);
//			purchased = new Integer(0);
//			errors = new Integer(0);
//			latencyCounter = new Integer(0);
//			completeRequestLatencyCounter = new Integer(0);
			
			

//			TrafficGeneratorThread r = new TrafficGeneratorThread(loadBalancerStub, numTheaters, requests,
//					averageLatency, completeReqAverageLatency, cancelled, errors, purchased, latencyCounter,
//					completeRequestLatencyCounter, origin, target, op, targetTheater, numClients, connector, NUM_SERVERS);

			TrafficGeneratorThread r = new TrafficGeneratorThread(loadBalancerStub, numTheaters, stats, origin, target, op, targetTheater, numClients, connector, NUM_SERVERS);			
			
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
			System.out.println("Num of requests made: " + stats[0] + "\n" +
								"Num of completed requests: " + stats[0] / 3 + "\n" + 
								"Num of purchases made: " + stats[1] + "\n" + 
								"Num of cancels made: " + stats[2] + "\n" + 
								"Num of errors gotten: " + stats[3] + "\n" + 
								"Average latenty per request: " + stats[4] + "\n" +
								"Average latency per completed request: " + stats[5] + "\n" +
								"Effective rate: " + stats[0]/(duration/1000));
			
//			System.out.println("Num of requests made: " + requests.intValue() + "\n" + "Num of completed requests: " + requests.intValue() / 3
//					+ "\n" + "Num of purchases made: " + purchased.intValue() + "\n" + "Num of cancels made: " + cancelled.intValue() + "\n"
//					+ "Num of errors gotten: " + errors.intValue() + "\n" + "Average latenty per request: " + averageLatency.intValue() + "\n"
//					+ "Average latency per completed request: " + completeReqAverageLatency.intValue() + "\n");

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
