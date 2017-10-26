package client;

import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.WideBoxIF;
import server.WideBoxImpl;

import java.rmi.RemoteException;
import java.util.Random;

public class TrafficGenThread extends Thread {

    //performance
    private int requests;
    private long averageLatency;
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

    public TrafficGenThread(WideBoxIF widebox, String targettheater, String origin,
                            String target, String op, int numClients, int numTheaters
            , int duration, long sleepRate) {
        this.wideBoxStub = widebox;
        this.targetTheater = targettheater;
        this.origin = origin;
        this.target = target;
        this.op = op;
        this.numClients = numClients;
        this.numTheaters = numTheaters;
        this.duration = duration;
        this.sleepRate = sleepRate;
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
                        SSQRequests();
                    }
                } else { //op = purchase
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        SSPRequests();
                    }
                }
            } else { //target = random
                if (op.equals("query")) {
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        SRQRequest(r);
                    }
                } else { //op = purchase
                    endTime = System.currentTimeMillis() + duration;
                    while (System.currentTimeMillis() < endTime) {
                        SRPRequest(r);
                    }
                }
            }
        } else { //origin = random
            if (target.equals("single")) {
                if (op.equals("query")) {
                    while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            RSQRequests(clientId);
                        }
                        clientId++;
                    }
                } else { //op = purchase
                    while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            RSPRequests(clientId);
                        }
                        clientId++;
                    }
                }
            } else { //target = random
                if (op.equals("query")) {
                    while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            RRQRequests(clientId, r);
                        }
                        clientId++;
                    }
                } else { //op = purchase
                    while (clientId <= numClients) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            RRPRequests(clientId, r);
                        }
                        clientId++;
                    }
                }
            }

        }
    }

    private void RRPRequests(int clientId, Random r) {
        System.out.println("Getting the theater names");
        String[] theaters;
        long latencyBeg = System.currentTimeMillis();
        long latencyEnd;
        int aux = r.nextInt(this.numTheaters + 1);
        try {

            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;
            System.out.println("Sending a query for theater " + theaters[aux]);

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[aux]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;

            System.out.println("I'm sleeping");
            Thread.sleep(sleepRate);
            if (m.getType() == MessageType.AVAILABLE) {
                System.out.println("I'm purchasing my pre-reservation\n" +
                        "------------------------------------");
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                addToLatency(latencyEnd - latencyBeg);
                this.requests++;
                this.purchased++;
            } else {
                this.errors++;
            }
        } catch (RemoteException e) {
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void RRQRequests(int clientId, Random r) {
        System.out.println("Getting the theater names");
        String[] theaters;
        long latencyBeg = System.currentTimeMillis();
        long latencyEnd;
        int aux = r.nextInt(this.numTheaters + 1);
        try {

            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;
            System.out.println("Sending a query for theater " + theaters[aux]);

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[aux]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;

            System.out.println("I'm sleeping");
            Thread.sleep(sleepRate);
            if (m.getType() == MessageType.AVAILABLE) {
                System.out.println("I'm cancelling my pre-reservation\n" +
                        "------------------------------------");
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                addToLatency(latencyEnd - latencyBeg);
                this.requests++;
                this.cancelled++;
            } else {
                this.errors++;
            }
        } catch (RemoteException e) {
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //does the same has SSPRequests........
    private void RSPRequests(int clientId) {
        System.out.println("Getting the theater names");
        String[] theaters;
        long latencyBeg = System.currentTimeMillis();
        long latencyEnd;
        try {
            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;
            System.out.println("Sending a query for theater " +
                    theaters[Integer.parseInt(targetTheater)]);

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;

            System.out.println("I'm sleeping");
            Thread.sleep(sleepRate);
            if (m.getType() == MessageType.AVAILABLE) {
                System.out.println("I'm purchasing my pre-reservation\n" +
                        "------------------------------------");
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                addToLatency(latencyEnd - latencyBeg);
                this.requests++;
                this.purchased++;
            } else {
                this.errors++;
            }
        } catch (RemoteException e) {
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //does the same as SSQRequests.......... :/
    private void RSQRequests(int clientId) {
        System.out.println("Getting the theater names");
        String[] theaters;
        long latencyBeg = System.currentTimeMillis();
        long latencyEnd;
        try {
            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;
            System.out.println("Sending a query for theater " +
                    theaters[Integer.parseInt(targetTheater)]);

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;

            System.out.println("I'm sleeping");
            Thread.sleep(sleepRate);
            if (m.getType() == MessageType.AVAILABLE) {
                System.out.println("I'm cancelling my pre-reservation\n" +
                        "------------------------------------");
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                addToLatency(latencyEnd - latencyBeg);
                this.requests++;
                this.cancelled++;
            } else {
                this.errors++;
            }
        } catch (RemoteException e) {
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void SRPRequest(Random r) {
        System.out.println("Getting the theater names");
        String[] theaters;
        long latencyBeg = System.currentTimeMillis();
        long latencyEnd;
        int aux = r.nextInt(this.numTheaters + 1);
        try {

            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;
            System.out.println("Sending a query for theater " + theaters[aux]);

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[aux]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;

            System.out.println("I'm sleeping");
            Thread.sleep(sleepRate);
            if (m.getType() == MessageType.AVAILABLE) {
                System.out.println("I'm purchasing my pre-reservation\n" +
                        "------------------------------------");
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                addToLatency(latencyEnd - latencyBeg);
                this.requests++;
                this.purchased++;
            } else {
                this.errors++;
            }
        } catch (RemoteException e) {
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void SRQRequest(Random r) {
        System.out.println("Getting the theater names");
        String[] theaters;
        long latencyBeg = System.currentTimeMillis();
        long latencyEnd;
        int aux = r.nextInt(this.numTheaters + 1);
        try {

            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;
            System.out.println("Sending a query for theater " + theaters[aux]);

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[aux]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;

            System.out.println("I'm sleeping");
            Thread.sleep(sleepRate);
            if (m.getType() == MessageType.AVAILABLE) {
                System.out.println("I'm cancelling my pre-reservation\n" +
                        "------------------------------------");
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[aux], m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                addToLatency(latencyEnd - latencyBeg);
                this.requests++;
                this.cancelled++;
            } else {
                this.errors++;
            }
        } catch (RemoteException e) {
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void SSPRequests() {
        System.out.println("Getting the theater names");
        String[] theaters;
        long latencyBeg = System.currentTimeMillis();
        long latencyEnd;
        try {
            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;
            System.out.println("Sending a query for theater " +
                    theaters[Integer.parseInt(targetTheater)]);

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;

            System.out.println("I'm sleeping");
            Thread.sleep(sleepRate);
            if (m.getType() == MessageType.AVAILABLE) {
                System.out.println("I'm purchasing my pre-reservation\n" +
                        "------------------------------------");
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                addToLatency(latencyEnd - latencyBeg);
                this.requests++;
                this.purchased++;
            } else {
                this.errors++;
            }
        } catch (RemoteException e) {
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void SSQRequests() {
        System.out.println("Getting the theater names");
        String[] theaters;
        long latencyBeg = System.currentTimeMillis();
        long latencyEnd;
        try {

            theaters = wideBoxStub.getNames();
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;
            System.out.println("Sending a query for theater " +
                    theaters[Integer.parseInt(targetTheater)]);

            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            addToLatency(latencyEnd - latencyBeg);
            this.requests++;

            System.out.println("I'm sleeping");
            Thread.sleep(sleepRate);
            if (m.getType() == MessageType.AVAILABLE) {
                System.out.println("I'm cancelling my pre-reservation\n" +
                        "------------------------------------");
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.cancel(theaters[Integer.parseInt(targetTheater)],
                        m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                addToLatency(latencyEnd - latencyBeg);
                this.requests++;
                this.cancelled++;
            } else {
                this.errors++;
            }
        } catch (RemoteException e) {
            this.errors++;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addToLatency(long diff) {
        this.averageLatency = this.averageLatency + ((diff - this.averageLatency) / this.latencyCounter);
    }


    public int getRequests() {
        return requests;
    }

    public long getLatency() {
        return averageLatency;
    }

}
