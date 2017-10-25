package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import auxiliary.ConfigHandler;
import auxiliary.WideBoxIF;

public class TrafficGenerator {


	public static void main(String[] args) {

		try {
			Registry reg = LocateRegistry.getRegistry(0);
			WideBoxIF wideBoxStub = (WideBoxIF) reg.lookup("WideBoxServer");


			ConfigHandler ch = new ConfigHandler();
			int numClients = ch.getNumClients();
			int numTheaters = ch.getNumClients();
			String op = ch.getOp();
			int duration = ch.getDuration();
			int rate = ch.getRate();
			long sleepRate = rate/1000;

			int clientId = 1;

			Random r = new Random();
			long endTime;
			int theaterId;
			if(op.equals("query")) {
				while(clientId <= numClients) {
					//hammered in
					endTime = System.currentTimeMillis() + duration; 
					while (System.currentTimeMillis() < endTime) {
						theaterId = r.nextInt(numTheaters);
						wideBoxStub.query("Theater"+Integer.toString(theaterId));
						Thread.sleep(sleepRate);
						
					}
					clientId++;
				}

			}else {
				while(clientId <= numClients) {
					clientId++;
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
