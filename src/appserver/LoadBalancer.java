package appserver;

import auxiliary.ConnectionHandler;


/**
 * PSD Project - Phase 2
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class LoadBalancer {

	public static void main(String args[]) throws Exception {

		String local_ip = "127.0.0.1";
		String zkIP = "localhost";
		String zkPort = "";

		if (args.length > 0) {
			local_ip = args[0];
			System.setProperty("java.rmi.server.hostname", local_ip);

			// args[1] = zookeeper IP
			zkIP = args[1];
			// args[2] = zookeeper Port
			zkPort = args[2];
		}
		String zkAddress = zkIP + ":" + zkPort;
		ConnectionHandler connector = new ConnectionHandler(zkAddress, ConnectionHandler.type.LoadBalancer);
		LoadBalancerImpl loadBalancer = new LoadBalancerImpl(connector);

		connector.register(loadBalancer, local_ip);
		System.out.println("LoadBalancer ready");
	}
}
