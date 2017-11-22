package client;

import auxiliary.ConfigHandler;
import auxiliary.WideBoxIF;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.*;

/**
 * PSD Project - Phase 1
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
    //requests, completed requests, purchased, cancelled, errors, average latency
    static volatile int [] stats = new int[6];

    public static void main(String[] args) {

        try {
            Registry reg;
            if (args.length > 0) {
                reg = LocateRegistry.getRegistry(args[0],5001);
            }
            else {
                reg = LocateRegistry.getRegistry(5001);
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
            int duration = ch.getDuration()*1000; //milliseconds
            int rate = ch.getRate();
            int numThread = ch.getNumThreads();
            long sleepRate = 1000;//going to be updated inside the thread     //rate/1000;

            long startTime = System.currentTimeMillis();
            //ExecutorService ex = Executors.newFixedThreadPool(numTheaters);
            ExecutorService ex = Executors.newSingleThreadScheduledExecutor();

            //ConcurrentLinkedQueue<Result> queue = new ConcurrentLinkedQueue<>();
            Result r;
            Future<Result> futureR = ex.submit(new Result(wideBoxStub,numTheaters,rate,
                    sleepRate,duration, stats));
            /*for (int i = 0; i < numThread; i++) {
                TrafficGenThread tgt = new TrafficGenThread(wideBoxStub,targetTheater,origin,target,
                        op,numClients,numTheaters,duration,sleepRate, rate);
                ex.execute(tgt);
            }
            ex.shutdown();
            try {
                ex.awaitTermination(duration * 3, TimeUnit.SECONDS);
            }
            catch (InterruptedException e){

            }
            */


            try {
                r = futureR.get();
                System.out.println(r.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }


            ex.shutdown();

            System.out.println("Runtime (s): " + ((System.currentTimeMillis() - startTime)/1000));
        } catch (RemoteException e1) {
            e1.printStackTrace();
        } catch (NotBoundException e1) {
            e1.printStackTrace();
        }
    }
}
