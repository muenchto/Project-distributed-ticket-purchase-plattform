package client;

import auxiliary.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
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
    static int latencyCounter = 0;
    static int completeRequestLatencyCounter = 0;
    static WideBoxIF wideBoxStub;

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
            //int numThread = ch.getNumThreads();
            long sleepRate = 1000 / rate;//going to be updated inside the thread     //rate/1000;


            //int numOfThreads = (int) Math.ceil(rate / 300);
            //int numOfTasks = rate / numOfThreads;

            long endTime = System.currentTimeMillis() + duration;
            long startTime = System.currentTimeMillis();

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    /*if (System.currentTimeMillis() >= endTime){
                        System.out.println("nfudsfd");
                        cancel();
                        //timer.cancel();
                        System.out.println("Num of requests made: " + stats[0] + "\n" +
                                "Num of completed requests: " + stats[0] / 3 + "\n" +
                                "Num of purchases made: " + stats[1] + "\n" +
                                "Num of cancels made: " + stats[2] + "\n" +
                                "Num of errors gotten: " + stats[3] + "\n" +
                                "Average latenty per request: " + stats[4] + "\n" +
                                "Average latency per completed request: " + stats[5] + "\n");
                        System.out.println("Runtime (s): " + ((System.currentTimeMillis() - startTime) / 1000));
                        System.exit(1);
                    }*/
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
                        latencyEnd = System.currentTimeMillis();
                        latencyCounter++;
                        latencydif = latencyEnd - latencyBeg;
                        mainRequestLatency += latencydif;
                        addToAverageLatency(latencydif);
                        synchronized (stats) {
                            stats[0]++;
                        }

                        String targetAppServer = getAppServerWithTheater(theaters, aux);
                        latencyBeg = System.currentTimeMillis();
                        wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
                        latencyEnd = System.currentTimeMillis();
                        latencyCounter++;
                        latencydif = latencyEnd - latencyBeg;
                        mainRequestLatency += latencydif;
                        addToAverageLatency(latencydif);


                        latencyBeg = System.currentTimeMillis();
                        Message m = wideBoxStub.query(theaterName);
                        //check for null if the theater doesnt exist in the message m

                        latencyEnd = System.currentTimeMillis();
                        latencyCounter++;
                        latencydif = latencyEnd - latencyBeg;
                        mainRequestLatency += latencydif;
                        addToAverageLatency(latencydif);
                        synchronized (stats) {
                            stats[0]++;
                        }


                        if (m.getType() == MessageType.AVAILABLE) {
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


            ScheduledExecutorService ex = Executors.newScheduledThreadPool(100);
            //ExecutorService ex = Executors.newCachedThreadPool();
            //ExecutorService ex = Executors.newSingleThreadScheduledExecutor();
/*
            TrafficGeneratorThread r = new TrafficGeneratorThread(loadBalancerStub, numTheaters, rate,
                                sleepRate, duration, stats, origin, target, op, targetTheater, numClients, zkAddress);
  */        //  long startTime = System.currentTimeMillis();

            //long endTime = System.currentTimeMillis() + duration;
            //Future<TrafficGeneratorThread> futureR = ex.submit(new TrafficGeneratorThread(loadBalancerStub, numTheaters, rate,
            //            sleepRate, duration, stats, origin, target, op, targetTheater, numClients, zkAddress));

            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    ex.shutdown();
                }
            }, duration, TimeUnit.MILLISECONDS);
            ex.scheduleAtFixedRate(timerTask,0,sleepRate,TimeUnit.MILLISECONDS);
/*
            while (!ex.isShutdown()) {
                ex.submit(timerTask);
                Thread.sleep(sleepRate);
            }*/
            while (!ex.isTerminated()) {
            }
            System.out.println("Num of requests made: " + stats[0] + "\n" +
                    "Num of completed requests: " + stats[0] / 3 + "\n" +
                    "Num of purchases made: " + stats[1] + "\n" +
                    "Num of cancels made: " + stats[2] + "\n" +
                    "Num of errors gotten: " + stats[3] + "\n" +
                    "Average latenty per request: " + stats[4] + "\n" +
                    "Average latency per completed request: " + stats[5] + "\n");


            System.out.println("Runtime (s): " + ((System.currentTimeMillis() - startTime) / 1000));
            System.exit(1);

            //}
            /*
            int rateCounter = 1;
            int rateAux = rate + 1;
            while(rateCounter % rateAux != 0) {
                if (rateCounter == rateAux){
                    rateCounter = 1;
                    continue;
                }
                ex.submit(timerTask);
                rateCounter++;
            }
            */
/*
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
                                "Average latenty per request: " + stats[4] + "\n" +
                                "Average latency per completed request: " + stats[5] + "\n";
                        System.out.println(s);
                    }
                }
            }, 0, 1000);

            //r = futureR.get();
*/

            //ex.shutdown();
 /*           while (!ex.isTerminated()) {
            }
            System.out.println("Num of requests made: " + stats[0] + "\n" +
                    "Num of completed requests: " + stats[0] / 3 + "\n" +
                    "Num of purchases made: " + stats[1] + "\n" +
                    "Num of cancels made: " + stats[2] + "\n" +
                    "Num of errors gotten: " + stats[3] + "\n" +
                    "Average latenty per request: " + stats[4] + "\n" +
                    "Average latency per completed request: " + stats[5] + "\n");


            System.out.println("Runtime (s): " + ((System.currentTimeMillis() - startTime) / 1000));
            System.exit(1);
*/


/*
            Timer timer = new Timer();
            timer.scheduleAtFixedRate( timerTask, 0,1000/rate);
            System.out.println("HFNUDISO");
            /*if (System.currentTimeMillis() >= endTime){
                System.out.println("nfudsfd");
                timerTask.cancel();
                timer.cancel();
                System.out.println("Num of requests made: " + stats[0] + "\n" +
                        "Num of completed requests: " + stats[0] / 3 + "\n" +
                        "Num of purchases made: " + stats[1] + "\n" +
                        "Num of cancels made: " + stats[2] + "\n" +
                        "Num of errors gotten: " + stats[3] + "\n" +
                        "Average latenty per request: " + stats[4] + "\n" +
                        "Average latency per completed request: " + stats[5] + "\n");
                System.out.println("Runtime (s): " + ((System.currentTimeMillis() - startTime) / 1000));
            }*/

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    private static synchronized void addToAverageLatency(long diff) {
        //System.out.print(diff+" ");
        stats[4] = Math.toIntExact(stats[4] + ((diff - stats[4]) / latencyCounter));
        //aux = Math.toIntExact(aux + ((diff - aux) / this.latencyCounter));
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
    }

}
