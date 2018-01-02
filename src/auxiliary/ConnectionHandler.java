package auxiliary;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * Created by tobiasmuench on 19.12.17.
 */
public class ConnectionHandler implements Watcher {

    final CountDownLatch connectedSignal = new CountDownLatch(1);

    public interface ConnectionWatcher {
        void connectionLost(String znode);
    }

    public enum type {
        DBServer,
        AppServer,
        LoadBalancer
    }

    private ZooKeeper zk;
    private String zkFolder;
    private String zkPath;
    private HashMap<String, ConnectionWatcher> watcherList;

    private int reg_port;
    private Registry local_registry;

    public int numServersAtStart;

    public ConnectionHandler(String zkAdress, ConnectionHandler.type serverType) {

        try {
            zk = new ZooKeeper(zkAdress, 1000, new Watcher() {

                public void process(WatchedEvent we) {

                    if (we.getState() == Event.KeeperState.SyncConnected) {
                        connectedSignal.countDown();
                    }
                }
            });
            connectedSignal.await();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        if (serverType != null) {

            if (serverType == type.AppServer) {
                zkFolder = "appserver";
                reg_port = 6000;
            } else if (serverType == type.DBServer) {
                zkFolder = "dbserver";
                reg_port = 5000;
            } else {
                zkFolder = "loadbalancer";
                reg_port = 7000;
            }
            zkPath = "/" + zkFolder;

            try {
                zk = new ZooKeeper(zkAdress, 1000, new Watcher() {

                    public void process(WatchedEvent we) {

                        if (we.getState() == Event.KeeperState.SyncConnected) {
                            connectedSignal.countDown();
                        }
                    }
                });
                connectedSignal.await();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

            try {
                //reset the folder to reset the node counter
                if (zk.exists(zkPath, false) != null && getAllNodes(zk, zkPath).size() == 0) {
                    zk.delete(zkPath, 0);
                    zk.create(zkPath, ("root of " + serverType).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
                //create a new server folder
                else if (zk.exists(zkPath, false) == null) {
                    zk.create(zkPath, ("root of " + serverType).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (KeeperException | InterruptedException e1) {
                e1.printStackTrace();
            }
            numServersAtStart = getAllNodes(zk, zkPath).size();
            try {
                local_registry = LocateRegistry.createRegistry(reg_port + numServersAtStart);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }

        }

        watcherList = new HashMap<String, ConnectionWatcher>();


        System.out.println("CONNECTION HANDLER: Registry and ZooKeeper connection established");
    }

    public Remote get(String serverName, String path) throws ConnectException {

        Registry remote_registry;

        String dbServerIP;
        String dbServerPort;
        try {
            byte[] zk_data = zk.getData(path + "/" + serverName, null, null);
            dbServerIP = new String(zk_data).split(":")[0];
            dbServerPort = new String(zk_data).split(":")[1];
            //System.out.println("CONNECTION HANDLER: trying to get " + serverName + " @Registry " + dbServerIP + ":" + dbServerPort);
            remote_registry = LocateRegistry.getRegistry(dbServerIP, Integer.parseInt(dbServerPort));
            //System.out.println("CONNECTION HANDLER: registered successfully at " + Arrays.toString(remote_registry.list()));
            return remote_registry.lookup(serverName);
        } catch (KeeperException | InterruptedException | RemoteException | NotBoundException e) {
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
        System.out.println("CONNECTION HANDLER: registered as " + zkFolder + numServersAtStart);

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
        System.out.println("CONNECTION HANDLER: created ZooKeeper node " + zkFolder + numServersAtStart);
    }

    public void setWatch(ConnectionWatcher object, String znode) {
        this.watcherList.put(znode, object);
        try {
            zk.getData(znode, this, null);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        String znode = event.getPath();
        if (event.getType() == Event.EventType.NodeDeleted){
            watcherList.get(znode).connectionLost(znode);
        }
    }


    public static List<String> getAllNodes(ZooKeeper zk, String path){
        Stat node;
        try {
            node = zk.exists(path, false);
            List<String> children = new ArrayList<>();
            if (node != null) {
                children = zk.getChildren(path, false);
                java.util.Collections.sort(children);
            }
            return children;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getNrOfNodesOnPath(String path){
        return getAllNodes(zk, path).size();
    }
}
