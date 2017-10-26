package client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import auxiliary.ConfigHandler;
import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.WideBoxIF;

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
            long sleepRate = rate / 1000;

            int clientId = 1;

            Random r = new Random();
            long endTime;

            if (origin.equals("single")) {
                if (target.equals("single")) {
                    if (op.equals("query")) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            System.out.println("Getting the theater names");
                            String [] theaters = wideBoxStub.getNames();
                            System.out.println("Sending a query for theater "+
                                    theaters[Integer.parseInt(targetTheater)]);
                            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
                            System.out.println("I'm sleeping");
                            Thread.sleep(sleepRate);
                            System.out.println("I'm cancelling my pre-reservation\n" +
                                    "------------------------------------");
                            wideBoxStub.cancel(m.getClientID());
                        }
                    } else { //operation = purchase
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            System.out.println("Getting the theater names");
                            String [] theaters = wideBoxStub.getNames();
                            System.out.println("Sending a query for theater "+
                                    theaters[Integer.parseInt(targetTheater)]);
                            Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
                            System.out.println("I'm sleeping");
                            Thread.sleep(sleepRate);
                            if (m.getType().equals(MessageType.AVAILABLE)) {
                                System.out.println("I'm accepting the pre-reservation on seat\n" +
                                        "------------------------------");
                                wideBoxStub.accept(m.getClientID());
                            }else {
                                System.out.println("Couldn't accept pre-reservation");
                            }
                        }
                    }
                } else { //target = random
                    if (op.equals("query")) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            System.out.println("Getting the theater names");
                            String [] theaters = wideBoxStub.getNames();
                            int aux = r.nextInt(numTheaters);
                            System.out.println("Sending a query for theater "+theaters[aux]);
                            Message m = wideBoxStub.query(theaters[aux]);
                            System.out.println("I'm sleeping");
                            Thread.sleep(sleepRate);
                            System.out.println("I'm canceling the pre-reservation\n" +
                                    "-----------------------------");
                            wideBoxStub.cancel(m.getClientID());
                        }
                    } else { //operation = purchase
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            System.out.println("Getting the theater names");
                            String [] theaters = wideBoxStub.getNames();
                            int aux = r.nextInt(numTheaters);
                            System.out.println("Sending a query for theater "+theaters[aux]);
                            Message m = wideBoxStub.query(theaters[aux]);
                            System.out.println("I'm sleeping");
                            Thread.sleep(sleepRate);
                            if (m.getType().equals(MessageType.AVAILABLE)) {
                                System.out.println("I'm accepting the pre-reservation\n" +
                                        "------------------------------");
                                wideBoxStub.accept(m.getClientID());
                            }else {
                                System.out.println("Couldn't accept pre-reservation\n" +
                                        "----------------------------");
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
                                System.out.println("Im client: "+clientId);
                                System.out.println("Getting the theater names");
                                String [] theaters = wideBoxStub.getNames();
                                System.out.println("Sending a query for theater "+
                                        theaters[Integer.parseInt(targetTheater)]);
                                Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
                                System.out.println("I'm sleeping");
                                Thread.sleep(sleepRate);
                                System.out.println("I'm cancelling my pre-reservation\n" +
                                        "------------------------------------");
                                wideBoxStub.cancel(m.getClientID());
                            }
                            clientId++;
                        }
                    } else { //operation = purchase
                        while (clientId <= numClients) {
                            endTime = System.currentTimeMillis() + duration;
                            while (System.currentTimeMillis() < endTime) {
                                System.out.println("Im client: "+clientId);
                                System.out.println("Getting the theater names");
                                String [] theaters = wideBoxStub.getNames();
                                System.out.println("Sending a query for theater "+
                                        theaters[Integer.parseInt(targetTheater)]);
                                Message m = wideBoxStub.query(theaters[Integer.parseInt(targetTheater)]);
                                System.out.println("I'm sleeping");
                                Thread.sleep(sleepRate);
                                if (m.getType().equals(MessageType.AVAILABLE)) {
                                    System.out.println("I'm accepting the pre-reservation\n" +
                                            "------------------------------");
                                    wideBoxStub.accept(m.getClientID());
                                }else {
                                    System.out.println("Couldn't accept pre-reservation\n" +
                                            "-----------------------------------");
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
                                System.out.println("Im client: "+clientId);
                                System.out.println("Getting the theater names");
                                String [] theaters = wideBoxStub.getNames();
                                int aux = r.nextInt(numTheaters);
                                System.out.println("Sending a query for theater "+theaters[aux]);
                                Message m = wideBoxStub.query(theaters[aux]);
                                System.out.println("I'm sleeping");
                                Thread.sleep(sleepRate);
                                System.out.println("I'm canceling the pre-reservation\n" +
                                        "-------------------------");
                                wideBoxStub.cancel(m.getClientID());
                            }
                            clientId++;
                        }
                    } else { //operation = purchase
                        while (clientId <= numClients) {
                            endTime = System.currentTimeMillis() + duration;
                            while (System.currentTimeMillis() < endTime) {
                                System.out.println("Im client: "+clientId);
                                System.out.println("Getting the theater names");
                                String [] theaters = wideBoxStub.getNames();
                                int aux = r.nextInt(numTheaters);
                                System.out.println("Sending a query for theater "+theaters[aux]);
                                Message m = wideBoxStub.query(theaters[aux]);
                                System.out.println("I'm sleeping");
                                Thread.sleep(sleepRate);
                                if (m.getType().equals(MessageType.AVAILABLE)) {
                                    System.out.println("I'm accepting the pre-reservation\n" +
                                            "------------------------------");
                                    wideBoxStub.accept(m.getClientID());
                                }else {
                                    System.out.println("Couldn't accept pre-reservation\n" +
                                            "--------------------------");
                                }
                            }
                            clientId++;
                        }
                    }
                }
            }

        } catch (RemoteException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (NotBoundException e1) {
            e1.printStackTrace();
        }
    }
}
