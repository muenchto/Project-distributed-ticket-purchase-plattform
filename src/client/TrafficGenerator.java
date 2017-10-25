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
            Registry reg = LocateRegistry.getRegistry(0);
            WideBoxIF wideBoxStub = (WideBoxIF) reg.lookup("WideBoxServer");


            ConfigHandler ch = new ConfigHandler();
            System.out.println(ch.toString());
            String origin = ch.getOrigin();
            String target = ch.getTarget();
            String targetTheater = ch.getTargetTheater();
            int numClients = ch.getNumClients();
            int numTheaters = ch.getNumClients();
            String op = ch.getOp();
            int duration = ch.getDuration();
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
                            Message m = wideBoxStub.query("Theater" + targetTheater);
                            Thread.sleep(sleepRate);
                            wideBoxStub.cancel(m.getClientID());
                        }
                    } else { //operation = purchase
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            Message m = wideBoxStub.query("Theater" + targetTheater);
                            Thread.sleep(sleepRate);
                            if (m.getType().equals(MessageType.ACCEPT_OK)) {
                                wideBoxStub.accept(m.getClientID());
                            }
                        }
                    }
                } else { //target = random
                    if (op.equals("query")) {
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            Message m = wideBoxStub.query("Theater" + Integer.toString(r.nextInt(numTheaters)));
                            Thread.sleep(sleepRate);
                            wideBoxStub.cancel(m.getClientID());
                        }
                    } else { //operation = purchase
                        endTime = System.currentTimeMillis() + duration;
                        while (System.currentTimeMillis() < endTime) {
                            Message m = wideBoxStub.query("Theater" + Integer.toString(numTheaters));
                            Thread.sleep(sleepRate);
                            if (m.getType().equals(MessageType.ACCEPT_OK)) {
                                wideBoxStub.accept(m.getClientID());
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
                                Message m = wideBoxStub.query("Theater" + targetTheater);
                                Thread.sleep(sleepRate);
                                wideBoxStub.cancel(m.getClientID());
                            }
                            clientId++;
                        }
                    } else { //operation = purchase
                        while (clientId <= numClients) {
                            endTime = System.currentTimeMillis() + duration;
                            while (System.currentTimeMillis() < endTime) {
                                Message m = wideBoxStub.query("Theater" + targetTheater);
                                Thread.sleep(sleepRate);
                                if (m.getType().equals(MessageType.ACCEPT_OK)) {
                                    wideBoxStub.accept(m.getClientID());
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
                                Message m = wideBoxStub.query("Theater" + Integer.toString(r.nextInt(numTheaters)));
                                Thread.sleep(sleepRate);
                                wideBoxStub.cancel(m.getClientID());
                            }
                            clientId++;
                        }
                    } else { //operation = purchase
                        while (clientId <= numClients) {
                            endTime = System.currentTimeMillis() + duration;
                            while (System.currentTimeMillis() < endTime) {
                                Message m = wideBoxStub.query("Theater" + Integer.toString(r.nextInt(numTheaters)));
                                Thread.sleep(sleepRate);
                                if (m.getType().equals(MessageType.ACCEPT_OK)) {
                                    wideBoxStub.accept(m.getClientID());
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
