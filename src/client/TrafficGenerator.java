package client;

import auxiliary.ConfigHandler;
import auxiliary.WideBoxIF;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TrafficGenerator {


    public static void main(String[] args) {

        try {
            Registry reg = LocateRegistry.getRegistry(5000);
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
            long sleepRate = rate/1000;


            ExecutorService ex = Executors.newFixedThreadPool(numTheaters);
            for (int i = 0; i < numThread; i++) {
                TrafficGenThread tgt = new TrafficGenThread(wideBoxStub,targetTheater,origin,target,
                        op,numClients,numTheaters,duration,sleepRate);
                if(tgt.getState().equals(Thread.State.TERMINATED)){
                    tgt.getLatency();
                }
                ex.execute(tgt);
            }
            ex.shutdown();
            while(!ex.isTerminated())
                ;



            //ex.
            /*
            for (int i = 0; i < numThread; i++) {
                TrafficGenThread tgt = new TrafficGenThread(wideBoxStub,targetTheater,origin,target,
                        op,numClients,numTheaters,rate,duration,sleepRate);
                tgt.start();
            }
            */


        } catch (RemoteException e1) {
            e1.printStackTrace();
        } catch (NotBoundException e1) {
            e1.printStackTrace();
        }
    }
}
