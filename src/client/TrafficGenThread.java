package client;

import auxiliary.Message;
import auxiliary.MessageType;
import server.WideBoxImpl;

import java.rmi.RemoteException;
import java.util.Random;

public class TrafficGenThread extends Thread {

    //performance
    private int requests;
    private int latency;
    private int discarted;
    private int servedResquestsRate;


    private WideBoxImpl wideBoxStub;
    private int clientId;
    private String targetTheater;
    private String origin;
    private String target;
    private String op;
    private int numClients;
    private int numTheaters;
    private int rate;
    private int duration;
    private long sleepRate;

    public TrafficGenThread(WideBoxImpl widebox, int clientId, String targettheater, String origin, String target,
                            String op, int numClients, int numTheaters, int rate, int duration,
                            long sleepRate) {
        this.wideBoxStub = widebox;
        this.clientId = clientId;
        this.targetTheater = targettheater;
        this.origin = origin;
        this.target = target;
        this.op = op;
        this.numClients = numClients;
        this.numTheaters = numTheaters;
        this.rate = rate;
        this.duration = duration;
        this.sleepRate = sleepRate;
    }

    @Override
    public void run() {
        Random r = new Random();
        long endTime;

        if (origin.equals("single")) {
            if (target.equals("single")){
                if (op.equals("query")){
                   endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            System.out.println("Getting the theater names");
                            String [] theaters = new String[0];
                            try {
                                theaters = wideBoxStub.getNames();

                                System.out.println("Sending a query for theater "+
                                    theaters[Integer.parseInt(targetTheater)]);
                                Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
                                System.out.println("I'm sleeping");
                                Thread.sleep(sleepRate);
                                System.out.println("I'm cancelling my pre-reservation\n" +
                                    "------------------------------------");
                                if(m.getType() != MessageType.FULL) {
                                    wideBoxStub.cancel(m.getClientID());
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                }else{

                }
            }else{

            }
        } else {

        }
    }


    public int getRequests() {
        return requests;
    }

    public int getLatency() {
        return latency;
    }

    public int getDiscarted() {
        return discarted;
    }

    public int getServedResquestsRate() {
        return servedResquestsRate;
    }
}
