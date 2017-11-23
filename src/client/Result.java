package client;

import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.WideBoxIF;

import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * PSD Project
 * 
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class Result implements Callable {
	
    private long latencyCounter;
    private long completeRequestLatencyCounter;
    private WideBoxIF wideBoxStub;
    private int numTheaters;
    private int rate;
    private long sleepRate;
    private int duration;
    private int[] stats;
    private int rateCounter;
    private String origin;
    private String target;
    private String op;
    private String targetTheater;
    private int numClients;

    public Result(WideBoxIF wideBoxStub, int numTheaters, int rate, long sleepRate, int duration, 
    		int[] stats, String origin, String target, String op, String targetTheater, int numClients) {
        this.wideBoxStub = wideBoxStub;
        this.numTheaters = numTheaters;
        this.rate = rate;
        this.sleepRate = sleepRate;
        this.duration = duration;
        this.stats = stats;
        this.rateCounter = 1;
        this.origin = origin;
        this.target = target;
        this.op = op;
        this.targetTheater = targetTheater;
        this.numClients = numClients;
    }

    @Override
    public Result call() throws Exception {
        long endTime;
        Random r = new Random();
        int clientId = 1;
        if (origin.equals("single")) {
            if (target.equals("single")) {
                if (op.equals("query")) {
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        while (this.rateCounter % (this.rate + 1) != 0) {
                            SSQRequest();
                        }
                        if (this.rateCounter == this.rate + 1) {
                            this.rateCounter = 1;
                        }
                    }
                } else { //op = purchase
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        while (this.rateCounter % (this.rate + 1) != 0) {
                            SSPRequest();
                        }
                        if (this.rateCounter == this.rate + 1) {
                            this.rateCounter = 1;
                        }
                    }
                }
            } else { //target = random
                if (op.equals("query")) {
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        while (this.rateCounter % (this.rate + 1) != 0) {
                            SRQRequest(r);
                        }
                        if (this.rateCounter == this.rate + 1) {
                            this.rateCounter = 1;
                        }
                    }
                } else { //op = purchase
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        while (this.rateCounter % (this.rate + 1) != 0) {
                            SRPRequest(r);
                        }
                        if (this.rateCounter == this.rate + 1) {
                            this.rateCounter = 1;
                        }
                    }
                }
            }
        } else { //origin = random
            if (target.equals("single")) {
                if (op.equals("query")) {
                    //while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            while (this.rateCounter % (this.rate + 1) != 0) {
                                RSQRequest(clientId);
                            }
                            if (this.rateCounter == this.rate + 1) {
                                this.rateCounter = 1;
                            }
                        }
                        //clientId++;
                    //}
                } else { //op = purchase
                    //while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            while (this.rateCounter % (this.rate + 1) != 0) {
                                RSPRequest(clientId);
                            }
                            if (this.rateCounter == this.rate + 1) {
                                this.rateCounter = 1;
                            }
                        }
                        //clientId++;
                    //}
                }
            } else { //target = random
                if (op.equals("query")) {
                    //while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            while (this.rateCounter % (this.rate + 1) != 0) {
                                RRQRequest(r, clientId);
                            }
                            if (this.rateCounter == this.rate + 1) {
                                this.rateCounter = 1;
                            }
                        }
                        //clientId++;
                    //}
                } else { //op = purchase
                    //while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() <= endTime) {
                            while (this.rateCounter % (this.rate + 1) != 0) {
                                RRPRequest(clientId, r);
                            }
                            if (this.rateCounter == this.rate + 1) {
                                this.rateCounter = 1;
                            }
                        }
                        //clientId++;
                    //}
                }
            }
        }
        return this;
    }

    public void SSQRequest(){
        String[] theaters;
        long latencyBeg;
        long latencyEnd;
        long mainRequestLatency = 0;
        long latencydif;
        try {
            latencyBeg = System.currentTimeMillis();
            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
                this.stats[0]++;
                this.stats[2]++;
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

    public void SSPRequest(){
        String[] theaters;
        long latencyBeg;
        long latencyEnd;
        long mainRequestLatency = 0;
        long latencydif;
        try {
            latencyBeg = System.currentTimeMillis();
            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
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

    public void SRQRequest(Random r){
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
            addToAverageLatency(latencydif);
            this.stats[0]++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[aux]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
                this.stats[0]++;
                this.stats[2]++;
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

    public void SRPRequest(Random r){
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
            addToAverageLatency(latencydif);
            this.stats[0]++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[aux]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;


            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
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

    public void RSQRequest(int clientId){ 
        String[] theaters;
        long latencyBeg;
        long latencyEnd;
        long mainRequestLatency = 0;
        long latencydif;
        try {
            latencyBeg = System.currentTimeMillis();
            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
                this.stats[0]++;
                this.stats[2]++;
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

    public void RSPRequest(int clientId){
        String[] theaters;
        long latencyBeg;
        long latencyEnd;
        long mainRequestLatency = 0;
        long latencydif;
        try {
            latencyBeg = System.currentTimeMillis();
            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
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

    public void RRQRequest(Random r, int clientId){
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
            addToAverageLatency(latencydif);
            this.stats[0]++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[aux]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
                this.stats[0]++;
                this.stats[2]++;
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

    public void RRPRequest(int clientId, Random r){
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
            addToAverageLatency(latencydif);
            this.stats[0]++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[aux]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            this.stats[0]++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
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



    private void addToAverageLatency(long diff) {
        this.stats[4] = Math.toIntExact(this.stats[4] + ((diff - this.stats[4]) / this.latencyCounter));
    }
    
    private void addToCompleteRequestLatency(long diff) {
        this.stats[4] = Math.toIntExact(this.stats[5] + ((diff - this.stats[5]) / this.completeRequestLatencyCounter));
    }

    @Override
    public String toString() {
        return "Num of requests made: " + this.stats[0] + "\n" +
            "Num of completed requests: " + this.stats[0] / 3 + "\n" +
            "Num of purchases made: " + this.stats[1] + "\n" +
            "Num of cancels made: " + this.stats[2] + "\n" +
            "Num of errors gotten: " + this.stats[3] + "\n" +
            "Average latenty per request: " + this.stats[4]+ "\n" +
            "Average latency per completed request: "+ this.stats[5]+"\n";
    }
}
