package client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import auxiliary.DataStorageIF;
import auxiliary.WideBoxIF;

public class FaultGenerator {

	public static void main(String[] args) {
		Registry wideboxRegistry;
		Registry dbserverRegistry;
		try {
            //get the widebox stub
			wideboxRegistry = LocateRegistry.getRegistry(5000);
			WideBoxIF wideboxStub = (WideBoxIF) wideboxRegistry.lookup("WideBoxServer");

			//get the dbserver stub
			dbserverRegistry = LocateRegistry.getRegistry(5000);
			DataStorageIF dbserverStub = (DataStorageIF) dbserverRegistry.lookup("dbServer");

			Scanner sc = new Scanner(System.in);

			String line;
			while (!((line = sc.nextLine()).equals("q"))) {
				switch (line) {
					case "db":
						dbserverStub.killServer();
						break;
					case "server":
                        wideboxStub.killServer();
                        break;
                    default:
                        break;
				}

			}

		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

}
