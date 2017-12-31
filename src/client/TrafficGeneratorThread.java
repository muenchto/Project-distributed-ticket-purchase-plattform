package client;

import auxiliary.ConnectionHandler;
import auxiliary.LoadBalancerIF;
import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.WideBoxIF;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * PSD Project
 * 
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class TrafficGeneratorThread implements Callable {
	
    private long latencyCounter;
    private long completeRequestLatencyCounter;
    private WideBoxIF wideBoxStub;
    private LoadBalancerIF loadBalancerStub;
    private ConnectionHandler connector;
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
    private int aux;
    private int numOfTasks;
    
	
    public TrafficGeneratorThread(LoadBalancerIF loadBalancerStub, int numTheaters, int rate, long sleepRate, int duration, 
    		int[] stats, String origin, String target, String op, String targetTheater, int numClients,
    		String zkAddress, int numOfTasks) {
        this.wideBoxStub = null;
        this.loadBalancerStub = loadBalancerStub;
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
        this.numOfTasks = numOfTasks;
        
        this.connector = new ConnectionHandler(zkAddress, ConnectionHandler.type.AppServer);
        
        //this.latencyCounter = 1;
    }

    @Override
    public TrafficGeneratorThread call() throws Exception {
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
                    int taskCounter = 0;
                    while ((System.currentTimeMillis() < endTime) 
                    		|| taskCounter != this.numOfTasks ) {
                        while ((this.rateCounter % (this.rate + 1) != 0) 
                        		|| taskCounter != this.numOfTasks) {
                            SRPRequest(r);
                            taskCounter++;
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
        this.stats[4] = aux;
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
        HashMap<String,String[]> theaters;
        long latencyBeg;
        long latencyEnd;
        long mainRequestLatency = 0;
        long latencydif;
        int aux = r.nextInt(this.numTheaters);
        String theaterName = "TheaterNr"+aux;
        try {
            latencyBeg = System.currentTimeMillis();
            theaters = loadBalancerStub.getNames();
            latencyEnd = System.currentTimeMillis();
            
            String targetAppServer = getAppServerWithTheater(theaters, aux);
            wideBoxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");
            
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            synchronized (stats) {
            	this.stats[0]++;
			}            
            latencyBeg = System.currentTimeMillis();
            Message m = wideBoxStub.query(theaterName);
            //check for null if the theater doesnt exist in the message m
            
            latencyEnd = System.currentTimeMillis();
            this.latencyCounter++;
            latencydif = latencyEnd - latencyBeg;
            mainRequestLatency += latencydif;
            addToAverageLatency(latencydif);
            synchronized (stats) {
            	this.stats[0]++;
			}


            if (m.getType() == MessageType.AVAILABLE) {
                latencyBeg = System.currentTimeMillis();
                wideBoxStub.accept(theaterName, m.getClientsSeat(), m.getClientID());
                latencyEnd = System.currentTimeMillis();
                this.latencyCounter++;
                latencydif = latencyEnd - latencyBeg;
                mainRequestLatency += latencydif;
                this.completeRequestLatencyCounter++;
                addToCompleteRequestLatency(mainRequestLatency);
                addToAverageLatency(latencydif);
                synchronized (stats) {
                	this.stats[0]++;
                	this.stats[1]++;
                }
            } else {
            	synchronized (stats) {
            		this.stats[3]++;
            	}
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
        	synchronized (stats) {
        		this.stats[3]++;
        	}
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

    private synchronized void addToAverageLatency(long diff) {
    	//System.out.print(diff+" ");
    	this.stats[4] = Math.toIntExact(this.stats[4] + ((diff - this.stats[4]) / this.latencyCounter));
    	//aux = Math.toIntExact(aux + ((diff - aux) / this.latencyCounter));
    }
    
    private synchronized void addToCompleteRequestLatency(long diff) {
        this.stats[5] = Math.toIntExact(this.stats[5] + ((diff - this.stats[5]) / this.completeRequestLatencyCounter));
    }
    
    private String getAppServerWithTheater(HashMap<String, String[]> theaters, int aux) {
    	String targetTheater = "TheaterNr"+aux;
    	for (Entry<String, String[]> e : theaters.entrySet()) {
			for (String s : e.getValue()) {
				if(s.equals(targetTheater)) {
					return e.getKey();
				}
			}
		}
    	return null;
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
