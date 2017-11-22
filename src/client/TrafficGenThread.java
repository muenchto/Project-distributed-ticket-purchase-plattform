package client;

import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.WideBoxIF;
import server.WideBoxImpl;

import java.rmi.RemoteException;
import java.util.Random;

/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class TrafficGenThread extends Thread {

    //performance
    private int requests;
    private int averageLatency;
    private int cancelled;
    private int purchased;
    private int errors;
    private int latencyCounter;

    private WideBoxIF wideBoxStub;
    private String targetTheater;
    private String origin;
    private String target;
    private String op;
    private int numClients;
    private int numTheaters;
    private int duration;
    private long sleepRate;
    private int rate;
    private int rateCounter;

    public TrafficGenThread(WideBoxIF widebox, String targettheater, String origin,
                            String target, String op, int numClients, int numTheaters
            , int duration, long sleepRate, int rate) {
        this.wideBoxStub = widebox;
        this.targetTheater = targettheater;
        this.origin = origin;
        this.target = target;
        this.op = op;
        this.numClients = numClients;
        this.numTheaters = numTheaters;
        this.duration = duration;
        this.sleepRate = sleepRate;
        this.rate = rate;
        this.rateCounter = 1;
    }

    @Override
    public void run() {
        Random r = new Random();
        long endTime;
        int clientId = 1;

        if (origin.equals("single")) {
            if (target.equals("single")) {
                if (op.equals("query")) {
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        while (this.rateCounter % (this.rate + 1) != 0) {
                            SSQRequests();
                            System.out.println(this.requests / 3);
                        }
                        if (this.rateCounter == this.rate + 1) {
                            this.rateCounter = 1;
                        }
                    }
                } else { //op = purchase
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        while (this.rateCounter % (this.rate + 1) != 0) {
                            SSPRequests();
                            System.out.println(this.requests / 3);
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
                            System.out.println(this.requests / 3);
                        }
                        if (this.rateCounter == this.rate + 1) {
                            this.rateCounter = 1;
                        }
                    }
                } else { //op = purchase
                    this.cancelled = 0;
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        while (this.rateCounter % (this.rate + 1) != 0) {
                            SRPRequest(r);
                            //System.out.println(this.requests / 3);
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
                    while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            while (this.rateCounter % (this.rate + 1) != 0) {
                               RSQRequests(clientId);
                                System.out.println(this.requests / 3);
                            }
                            if (this.rateCounter == this.rate + 1) {
                                this.rateCounter = 1;
                            }
                        }
                        clientId++;
                    }
                } else { //op = purchase
                    while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            while (this.rateCounter % (this.rate + 1) != 0) {
                                RSPRequests(clientId);
                                System.out.println(this.requests / 3);
                            }
                            if (this.rateCounter == this.rate + 1) {
                                this.rateCounter = 1;
                            }
                        }
                        clientId++;
                    }
                }
            } else { //target = random
                if (op.equals("query")) {
                    while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            while (this.rateCounter % (this.rate + 1) != 0) {
                                RRQRequests(clientId, r);
                                System.out.println(this.requests / 3);
                            }
                            if (this.rateCounter == this.rate + 1) {
                                this.rateCounter = 1;
                            }
                        }
                        clientId++;
                    }
                } else { //op = purchase
                    while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() <= endTime) {
                            while (this.rateCounter % (this.rate + 1) != 0) {
                                RRPRequests(clientId, r);
                                System.out.println(this.requests / 3);
                            }
                            if (this.rateCounter == this.rate + 1) {
                                this.rateCounter = 1;
                            }
                        }
                        clientId++;
                    }
                }
            }
        }
        System.out.println(this.requests/3);
    }

    private void RRPRequests(int clientId, Random r) {
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

    private void RRQRequests(int clientId, Random r) {
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
                wideBoxStub.cancel(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                addToLatency(latencydif);
                this.requests++;
                this.cancelled++;
            } else {
                this.errors++;
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
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //does the same has SSPRequests........
    private void RSPRequests(int clientId) {
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
            addToLatency(latencydif);
            this.requests++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToLatency(latencydif);
            this.requests++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
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

    //does the same as SSQRequests.......... :/
    private void RSQRequests(int clientId) {
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
            addToLatency(latencydif);
            this.requests++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToLatency(latencydif);
            this.requests++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                addToLatency(latencydif);
                this.requests++;
                this.cancelled++;
            } else {
                this.errors++;
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
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void SRPRequest(Random r) {
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

    private void SRQRequest(Random r) {
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
                wideBoxStub.cancel(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                addToLatency(latencydif);
                this.requests++;
                this.cancelled++;
            } else {
                this.errors++;
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
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void SSPRequests() {
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
            addToLatency(latencydif);
            this.requests++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToLatency(latencydif);
            this.requests++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
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

    private void SSQRequests() {
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
            addToLatency(latencydif);
            this.requests++;

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToLatency(latencydif);
            this.requests++;

            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                addToLatency(latencydif);
                this.requests++;
                this.cancelled++;
            } else {
                this.errors++;
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
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addToLatency(long diff) {
        this.averageLatency = Math.toIntExact(this.averageLatency + ((diff - this.averageLatency) / this.latencyCounter));
    }


    public int getRequests() {
        return requests;
    }

    public long getLatency() {
        return averageLatency;
    }

}
