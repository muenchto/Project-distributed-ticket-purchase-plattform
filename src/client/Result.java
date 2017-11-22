package client;

import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.WideBoxIF;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.Callable;

public class Result implements Callable {

    private int requests;
    private int averageLatency;
    private int cancelled;
    private int purchased;
    private int errors;

    private long latencyCounter;
    private WideBoxIF wideBoxStub;
    private int numTheaters;
    private int rate;
    private long sleepRate;
    private int duration;
    private int[] stats;
    private int rateCounter;

    public Result(WideBoxIF wideBoxStub, int numTheaters, int rate, long sleepRate, int duration, int[] stats) {
        this.wideBoxStub = wideBoxStub;
        this.numTheaters = numTheaters;
        this.rate = rate;
        this.sleepRate = sleepRate;
        this.duration = duration;
        this.stats = stats;
        this.rateCounter = 1;
        this.cancelled = 0;
    }

    @Override
    public Result call() throws Exception {
        long endTime = System.currentTimeMillis() + duration;
        while (System.currentTimeMillis() < endTime) {
            while (this.rateCounter % (this.rate + 1) != 0) {
                Random r = new Random();
                String[] theaters;
                long latencyBeg;
                long latencyEnd;
                long mainRequestLatency = 0;
                long latencydif;
                int aux = r.nextInt(this.numTheaters);
                try {
                    latencyBeg = System.currentTimeMillis();
                    theaters = wideBoxStub.getNames();
                    latencyEnd = System.currentTimeMillis();
                    this.latencyCounter++;
                    latencydif = latencyEnd - latencyBeg;
                    mainRequestLatency += latencydif;
                    addToLatency(latencydif);
                    this.requests++;

                    latencyBeg = System.currentTimeMillis();
                    Message m = wideBoxStub.query(theaters[aux]);
                    latencyEnd = System.currentTimeMillis();
                    this.latencyCounter++;
                    latencydif = latencyEnd - latencyBeg;
                    mainRequestLatency += latencydif;
                    addToLatency(latencydif);
                    this.requests++;

                    if (m.getType() == MessageType.AVAILABLE) {
                        latencyBeg = System.currentTimeMillis();
                        wideBoxStub.accept(theaters[aux], m.getClientsSeat(), m.getClientID());
                        latencyEnd = System.currentTimeMillis();
                        this.latencyCounter++;
                        latencydif = latencyEnd - latencyBeg;
                        mainRequestLatency += latencydif;
                        addToLatency(latencydif);
                        this.requests++;
                        this.purchased++;
                    } else {
                        this.errors++;
                    }

                    if (mainRequestLatency <= this.sleepRate) {
                        this.sleepRate -= mainRequestLatency;
                    }

                    if (this.rateCounter == this.rate) {
                        if (this.sleepRate > 0) {
                            //System.out.println("sleeping for "+sleepRate);
                            Thread.sleep(this.sleepRate);
                        }
                        this.sleepRate = 1000;
                    }
                    this.rateCounter++;

                } catch (RemoteException e) {
                    this.errors++;
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (this.rateCounter == this.rate + 1) {
                this.rateCounter = 1;
            }
        }
        return this;
    }

    private void addToLatency(long diff) {
        this.averageLatency = Math.toIntExact(this.averageLatency + ((diff - this.averageLatency) / this.latencyCounter));
    }

    @Override
    public String toString() {
        return "Num of requests made: " + this.requests + "\n" +
                "Num of completed requests: " + this.requests / 3 + "\n" +
                "Num of purchases made: " + this.purchased + "\n" +
                "Num of cancels made: " + this.cancelled + "\n" +
                "Num of errors gotten: " + this.errors + "\n" +
                "Average latenty per request: " + this.averageLatency + "\n";
    }
}
