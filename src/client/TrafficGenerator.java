package client;

import auxiliary.ConfigHandler;
import auxiliary.WideBoxIF;

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
            Registry reg;
            if (args.length > 0) {
                reg = LocateRegistry.getRegistry(args[0], 5000);
            } else {
                reg = LocateRegistry.getRegistry(5000);
            }
            WideBoxIF wideBoxStub = (WideBoxIF) reg.lookup("WideBoxServer");


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

            Result r;
            long startTime = System.currentTimeMillis();
            final Future<Result> futureR = ex.submit(new Result(wideBoxStub, numTheaters, rate,
                    sleepRate, duration, stats, origin, target, op, targetTheater, numClients));
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
                            System.out.println(s);
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
        } catch (RemoteException e1) {
            e1.printStackTrace();
        } catch (NotBoundException e1) {
            e1.printStackTrace();
        }
    }
}
