package client;

import auxiliary.ConfigHandler;
import auxiliary.ConnectionHandler;
import auxiliary.LoadBalancerIF;
import auxiliary.WideBoxIF;
import server.LoadBalancer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class TrafficGenerator {
    /*
    volatile int requests;
    volatile int averageLatency;
    volatile int cancelled;
    volatile int purchased;
    volatile int errors;
*/
    //requests, purchased, cancelled, errors, average latency, complete request average latency
    static volatile int[] stats = new int[6];

    public static void main(String[] args) {

        try {
            String zkIP = "localhost";
            String zkPort = "";

            if (args.length > 0) {
                //args[0] = own IP
                System.setProperty("java.rmi.server.hostname", args[0]);

                //args[1] = zookeeper IP
                zkIP = args[1];
                //args[2] = zookeeper Port
                zkPort = args[2];
            }
            String zkAddress = zkIP + ":" + zkPort;
            ConnectionHandler connector = new ConnectionHandler(zkAddress, ConnectionHandler.type.LoadBalancer);
            LoadBalancerIF loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer0", "/loadbalancer");
            
            /*
            ConnectionHandler connector = new ConnectionHandler(zkAddress, ConnectionHandler.type.AppServer);
            WideBoxIF wideBoxStub = (WideBoxIF) connector.get("loadbalancer0", "/appserver");
			*/

            ConfigHandler ch = new ConfigHandler();
            System.out.println(ch.toString());
            String origin = ch.getOrigin();
            String target = ch.getTarget();
            String targetTheater = ch.getTargetTheater();
            int numClients = ch.getNumClients();
            int numTheaters = ch.getNumTheaters();
            String op = ch.getOp();
            int duration = ch.getDuration() * 1000; //milliseconds
            int rate = ch.getRate();
            int numThread = ch.getNumThreads();
            long sleepRate = 1000;//going to be updated inside the thread     //rate/1000;

            //ExecutorService ex = Executors.newFixedThreadPool(numTheaters);
            ExecutorService ex = Executors.newSingleThreadScheduledExecutor();

            TrafficGeneratorThread r;
            long startTime = System.currentTimeMillis();
            final Future<TrafficGeneratorThread> futureR = ex.submit(new TrafficGeneratorThread(loadBalancerStub, numTheaters, rate,
                    sleepRate, duration, stats, origin, target, op, targetTheater, numClients, zkAddress));
            try {
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!futureR.isDone()) {
                            String s = "Num of requests made: " + stats[0] + "\n" +
                                    "Num of completed requests: " + stats[0] / 3 + "\n" +
                                    "Num of purchases made: " + stats[1] + "\n" +
                                    "Num of cancels made: " + stats[2] + "\n" +
                                    "Num of errors gotten: " + stats[3] + "\n" +
                                    "Average latenty per request: " + stats[4]+ "\n" +
                                    "Average latency per completed request: "+ stats[5]+"\n";
                            //System.out.println(s);
                        }
                    }
                }, 0, 1000);

                r = futureR.get();
                System.out.println(r.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            ex.shutdown();

            System.out.println("Runtime (s): " + ((System.currentTimeMillis() - startTime) / 1000));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
