package auxiliary;

import org.apache.zookeeper.*;
import zookeeperlib.ZKUtils;
import zookeeperlib.ZooKeeperConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;


/**
 * Created by tobiasmuench on 19.12.17.
 */
public class ConnectionHandler {

    final CountDownLatch connectedSignal = new CountDownLatch(1);

    public interface ZKConnection {
        void listen(WatchedEvent event);
    }

    public enum type {
        DBServer,
        AppServer
    }

    private ZooKeeper zk;
    ZooKeeperConnection zkconn;
    String zkFolder;
    String zkPath;

    int reg_port;
    Registry local_registry;

    public int numServersAtStart;

    public ConnectionHandler(String zkAdress, ConnectionHandler.type serverType) {

        if (serverType == type.AppServer) {
            zkFolder = "appserver";
            reg_port = 6000;
        } else {
            zkFolder = "dbserver";
            reg_port = 5000;
        }
        zkPath = "/" + zkFolder;

        try {
            zk = new ZooKeeper(zkAdress,1000, new Watcher() {

                public void process(WatchedEvent we) {

                    if (we.getState() == Event.KeeperState.SyncConnected) {
                        connectedSignal.countDown();
                    }
                }
            });
            connectedSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //reset the folder to reset the node counter
            if (zk.exists(zkPath, false) != null && ZKUtils.getAllNodes(zk, zkPath).size() == 0) {
                zk.delete(zkPath, 0);
                zk.create(zkPath, ("root of "+ serverType).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            //create a new server folder
            else if (zk.exists(zkPath, false) == null) {
                zk.create(zkPath, ("root of "+serverType).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }
        catch (KeeperException | InterruptedException e1) {
            e1.printStackTrace();
        }

        numServersAtStart = ZKUtils.getAllNodes(zk, zkPath).size();

        try {
            local_registry = LocateRegistry.createRegistry(reg_port + numServersAtStart);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        System.out.println("Registry and ZooKeeper connection established");
    }

    public Remote get(String serverName, String path) {

        Registry remote_registry;

        String dbServerIP;
        String dbServerPort;
        try {
            byte[] zk_data = zk.getData(path + "/" + serverName, null, null);
            dbServerIP = new String(zk_data).split(":")[0];
            dbServerPort = new String(zk_data).split(":")[1];
            System.out.println("ConnectionHandler trying to get " + serverName + " @Registry " + dbServerIP + ":" + dbServerPort);
            remote_registry = LocateRegistry.getRegistry(dbServerIP, Integer.parseInt(dbServerPort));
            System.out.println("reg at " + Arrays.toString(remote_registry.list()));
            return remote_registry.lookup(serverName);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return null; //TODO: make it better
    }

        // Method to disconnect from zookeeper server
    public void close() throws InterruptedException {
        zk.close();
    }

    public void register(Remote obj) {

        try {
            local_registry.rebind(zkFolder + numServersAtStart, obj);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("registered as " + zkFolder + numServersAtStart);

        try {
            zk.create(zkPath + "/" + zkFolder + numServersAtStart,
                    (InetAddress.getLocalHost().getHostAddress() + ":" + (reg_port + numServersAtStart)).getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            //zk.getChildren(zkPath + "/" + serverName, true);
        } catch (UnknownHostException | KeeperException | InterruptedException e1) {
            if (e1.getClass().equals(KeeperException.class)) {
                e1.printStackTrace();
            } else {
                e1.printStackTrace();
            }
        }
        System.out.println("created ZooKeeper node " + zkFolder + numServersAtStart);
    }
}
