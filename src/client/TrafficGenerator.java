package client;

import auxiliary.ConfigHandler;
import auxiliary.WideBoxIF;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class TrafficGenerator {


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


            ExecutorService ex = Executors.newFixedThreadPool(numTheaters);
            for (int i = 0; i < numThread; i++) {
                TrafficGenThread tgt = new TrafficGenThread(wideBoxStub,targetTheater,origin,target,
                        op,numClients,numTheaters,duration,sleepRate, rate);
                ex.execute(tgt);
            }
            ex.shutdown();


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
