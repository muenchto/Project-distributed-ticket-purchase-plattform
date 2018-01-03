package client;

import auxiliary.*;
import java.util.concurrent.*;

/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class TrafficGenerator {

	// requests, purchased, cancelled, errors, average latency, complete request
	// average latency, average latency counter, comp req latency counter
	//static int[] stats = new int[8];

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
//			final ConnectionHandler connector2 = new ConnectionHandler(zkAddress, null);
//			final LoadBalancerIF loadBalancerStub2 = (LoadBalancerIF) connector.get("loadbalancer0", "/loadbalancer");
			int[] stats = new int[8];
//			int[] stats2 = new int[8];

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


			TrafficGeneratorThread task = new TrafficGeneratorThread(loadBalancerStub, numTheaters, stats, origin, target, op, targetTheater, numClients, connector, NUM_SERVERS);
			//TrafficGeneratorThread task2 = new TrafficGeneratorThread(loadBalancerStub2, numTheaters, stats2, origin, target, op, targetTheater, numClients, connector2, NUM_SERVERS);
			
			final ScheduledExecutorService ex = Executors.newScheduledThreadPool(8);
			//final ScheduledExecutorService ex2 = Executors.newScheduledThreadPool(5);

			Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
				@Override
				public void run() {
					ex.shutdown();
					//ex2.shutdown();
				}
			}, duration, TimeUnit.MILLISECONDS);
			
			ex.scheduleAtFixedRate(task, 0, sleepRate, TimeUnit.MILLISECONDS);
//			ex2.scheduleAtFixedRate(task2, 0, sleepRate*2, TimeUnit.MILLISECONDS);
			
			while (!ex.isTerminated()/* && !ex2.isTerminated()*/) {
			}
			
			System.out.println("Thread Pool 1:\nNum of requests made: " + stats[0] + "\n" +
					"Num of completed requests: " + stats[0] / 3 + "\n" + 
					"Num of purchases made: " + stats[1] + "\n" + 
					"Num of cancels made: " + stats[2] + "\n" + 
					"Num of errors gotten: " + stats[3] + "\n" + 
					"Average latenty per request: " + stats[4] + "\n" +
					"Average latency per completed request: " + stats[5] + "\n" +
					"Effective rate: " + stats[0]/(duration/1000) + "\n");
			
			
//			System.out.println("Thread Pool 2:\nNum of requests made: " + stats2[0] + "\n" +
//					"Num of completed requests: " + stats2[0] / 3 + "\n" + 
//					"Num of purchases made: " + stats2[1] + "\n" + 
//					"Num of cancels made: " + stats2[2] + "\n" + 
//					"Num of errors gotten: " + stats2[3] + "\n" + 
//					"Average latenty per request: " + stats2[4] + "\n" +
//					"Average latency per completed request: " + stats2[5] + "\n" +
//					"Effective rate: " + stats2[0]/(duration/1000) + "\n");
//			
//			
//			System.out.println("Total:\nNum of requests made: " + (stats[0] + stats2[0]) + "\n" +
//								"Num of completed requests: " + (stats[0] / 3  + stats2[0] / 3) + "\n" + 
//								"Num of purchases made: " + (stats[1] + stats2[1]) + "\n" + 
//								"Num of cancels made: " + (stats[2] + stats2[2]) + "\n" + 
//								"Num of errors gotten: " + (stats[3] + stats2[3]) + "\n" + 
//								"Average latenty per request: " + (stats[4] + stats2[4]) + "\n" +
//								"Average latency per completed request: " + (stats[5] + stats2[5]) + "\n" +
//								"Effective rate: " + (stats[0]/(duration/1000) + stats2[0]/(duration/1000)) + "\n");

			System.out.println("Runtime (s): " + ((System.currentTimeMillis() - startTime) / 1000));
			System.exit(1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}