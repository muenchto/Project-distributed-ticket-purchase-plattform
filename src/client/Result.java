package client;

import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.WideBoxIF;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.Callable;

public class Result implements Callable {
/*
*/
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
        this.stats[2] = 0;
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
                    this.stats[0]++;

                    latencyBeg = System.currentTimeMillis();
                    Message m = wideBoxStub.query(theaters[aux]);
                    latencyEnd = System.currentTimeMillis();
                    this.latencyCounter++;
                    latencydif = latencyEnd - latencyBeg;
                    mainRequestLatency += latencydif;
                    addToLatency(latencydif);
                    this.stats[0]++;


                    if (m.getType() == MessageType.AVAILABLE) {
                        latencyBeg = System.currentTimeMillis();
                        wideBoxStub.accept(theaters[aux], m.getClientsSeat(), m.getClientID());
                        latencyEnd = System.currentTimeMillis();
                        this.latencyCounter++;
                        latencydif = latencyEnd - latencyBeg;
                        mainRequestLatency += latencydif;
                        addToLatency(latencydif);
                        this.stats[0]++;
                        this.stats[1]++;
                    } else {
                        this.stats[3]++;
                    }

                    if (mainRequestLatency <= this.sleepRate) {
                        this.sleepRate -= mainRequestLatency;
                    }

                    if (this.rateCounter == this.rate) {
                        if (this.sleepRate > 0) {
                            Thread.sleep(this.sleepRate);
                        }
                        this.sleepRate = 1000;
                    }
                    this.rateCounter++;

                } catch (RemoteException e) {
                    this.stats[3]++;
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
        this.stats[4] = Math.toIntExact(this.stats[4] + ((diff - this.stats[4]) / this.latencyCounter));
    }

    @Override
    public String toString() {
        return "Num of requests made: " + this.stats[0] + "\n" +
                "Num of completed requests: " + this.stats[0] / 3 + "\n" +
                "Num of purchases made: " + this.stats[1] + "\n" +
                "Num of cancels made: " + this.stats[2] + "\n" +
                "Num of errors gotten: " + this.stats[3] + "\n" +
                "Average latenty per request: " + this.stats[4]+ "\n";
    }
}
