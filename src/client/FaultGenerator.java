package client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import appserver.LoadBalancer;
import auxiliary.ConnectionHandler;
import auxiliary.DataStorageIF;
import auxiliary.LoadBalancerIF;
import auxiliary.WideBoxIF;

public class FaultGenerator {

	public static void main(String[] args) {

		try {
			String zkIP = "localhost";
			String zkPort = "";

			if (args.length > 0) {
				// args[0] = own IP
				System.setProperty("java.rmi.server.hostname", args[0]);

				// args[1] = zookeeper IP
				zkIP = args[1];
				// args[2] = zookeeper Port
				zkPort = args[2];
			}
			String zkAddress = zkIP + ":" + zkPort;
			ConnectionHandler connector = new ConnectionHandler(zkAddress, null);


			Scanner sc = new Scanner(System.in);

			String serverChoice;
			int number;
			boolean loop = true;
			while (loop) {
				System.out.println("Enter Server to kill (db | app | lb):");
				serverChoice = sc.nextLine();
				System.out.println("Specify the number");
				number = Integer.parseInt(sc.nextLine());
				switch (serverChoice) {
					case "db":
						DataStorageIF dbserverStub = (DataStorageIF) connector.get("dbserver"+number, "/dbserver");
						dbserverStub.killServer();
						break;
					case "app":
						WideBoxIF wideboxStub = (WideBoxIF) connector.get("appserver"+number, "/appserver");
						wideboxStub.killServer();
						break;
					case "lb":
						LoadBalancerIF lbStub = (LoadBalancerIF) connector.get("loadbalancer"+number, "/loadbalancer");
						lbStub.killServer();
						break;
					case "q":
						loop = false;
						break;
                    default:
                        break;
				}

			}

		} catch (RemoteException e) {
			//e.printStackTrace();
		}
	}

}
