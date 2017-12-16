package server;

import auxiliary.*;
import zookeeperLib.ZooKeeperConnection;

import com.stoyanr.evictor.map.ConcurrentHashMapWithTimedEviction;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;



/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class WideBoxImpl extends UnicastRemoteObject implements WideBoxIF {

    private final long EXPIRING_DURATION = 15000;

    private Registry registry;
    private DataStorageIF dataStorageStub;

    private int clientCounter;

    private ConcurrentHashMap<String, ConcurrentHashMapWithTimedEviction<String, Integer>> reservedSeats;

    private LinkedHashMap theaters;

    private boolean dbServerLocalMode;

    // create static instance for zookeeper class.
    private static ZooKeeper zk;

    // create static instance for ZooKeeperConnection class.
    private static ZooKeeperConnection zkconn;

    //the number of Servers that has been started before this one
    private int numServersAtStart;

    public int getNumServersAtStart() {
        return numServersAtStart;
    }

    public WideBoxImpl(String ZKadress) throws RemoteException {

        dbServerLocalMode = false;

        System.out.println("Widebox starting");

        this.reservedSeats = new ConcurrentHashMap<String, ConcurrentHashMapWithTimedEviction<String, Integer>>(1500);
        this.theaters = new LinkedHashMap<String, Theater>();

        zkconn = new ZooKeeperConnection();
        try {
            zk = zkconn.connect(ZKadress);
			zk.create("/appserver", "root of appservers".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			numServersAtStart = ZKUtils.getAllNodes(zk, "/appserver").size();
			zk.create("appserver/appserver",
					(InetAddress.getLocalHost().getHostAddress() + ":" + (5000 + numServersAtStart)).getBytes(),
					ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL_SEQUENTIAL);
			zk.getChildren("/dbserver", true);
			
		} catch (IOException | KeeperException | InterruptedException e1) {
            if (e1.getClass().equals(KeeperException.class)) {
                System.out.println("ZOOKEEPER: /appserver already exists");
            }else {
            	e1.printStackTrace();
            }
		}

        if (!dbServerLocalMode) {
            try {
                String dbServerIP = zk.getData("/dbserver/dbserver0", false, null).toString().split(":")[0];

                registry = LocateRegistry.getRegistry(dbServerIP, 5000);

                System.out.println("WideBoxImpl got the registry at " + dbServerIP);

                dataStorageStub = (DataStorageIF) registry.lookup("dbServer0");
                System.err.println("WideBoxImpl found DBServer");

            } catch (Exception e) {
                System.err.println("WideBoxImpl exception: " + e.toString());
                e.printStackTrace();
            }
        }else {
            for (int i = 0; i < 1500; i++) {
                this.theaters.put("TheaterNr" + i, new Theater("TheaterNr" + i));
            }
            this.clientCounter = 0;
        }
        System.out.println("WideBox ready..");

    }


    public String[] getNames() throws RemoteException {
        if (dbServerLocalMode) {
            java.util.Set keys = this.theaters.keySet();
            String[] names = (String[]) keys.toArray(new String[keys.size()]);
            return names;

        } else {
        	System.out.println("getNames");
            String[] temp = dataStorageStub.getTheaterNames();
            return temp;
        }
    }

    public Message query(String theaterName) throws RemoteException {

        //if the theater is queried the first time, the name is added to the HasMap
        //and a new ExpiringMap is created for the Seats and the Expirer is started for this seat map
        if (!reservedSeats.containsKey(theaterName)) {

            ConcurrentHashMapWithTimedEviction<String, Integer> expiringSeatMap =
                    new ConcurrentHashMapWithTimedEviction<>();
            reservedSeats.put(theaterName, expiringSeatMap);
        }

        Theater theater;
        synchronized (reservedSeats.get(theaterName)) {
            if (dbServerLocalMode) {
                theater = (Theater) this.theaters.get(theaterName);
                theater = theater.clone();
            } else {
                theater = dataStorageStub.getTheater(theaterName);
            }

            if (theater.status == TheaterStatus.FULL) {
                return new Message(MessageType.FULL);
            }
            //add all reserved seats from one theater to the theaterObject as reserved
            for (String s : reservedSeats.get(theaterName).keySet()) {
                theater.setSeatToReserved(s);
            }
            //reserve a new Seat for the client
            Seat seat = theater.reserveSeat();

            if (seat == null) {
                //TODO: this is not entirly correct because maybe another client will delete its reservation and therefore
                //the theater will be free again
                return new Message(MessageType.FULL);
            }

            clientCounter++;
            int clientID = clientCounter;

            reservedSeats.get(theater.theaterName).put(seat.getSeatName(), clientID, EXPIRING_DURATION);


            System.out.println("QUERY from " + clientCounter + ": reservedSeats @ " + theater.theaterName + ": " + reservedSeats.get(theater.theaterName).keySet());

            return new Message(MessageType.AVAILABLE, theater.seats, seat, clientID);
        }
    }

    public Message reserve(String theaterName, Seat old_seat, Seat wish_seat, int clientID) throws RemoteException {

        Theater theater;
        synchronized (reservedSeats.get(theaterName)) {
            if (dbServerLocalMode) {
                theater = (Theater) this.theaters.get(theaterName);
                theater = theater.clone();
            } else {
                theater = dataStorageStub.getTheater(theaterName);
            }


            //add all reserved seats from one theater to the theaterObject as reserved
            for (String s : reservedSeats.get(theaterName).keySet()) {
                theater.setSeatToReserved(s);
            }
            //try to reserve a new Seat for the client
            Seat new_seat = theater.reserveSeat(wish_seat);

            if (new_seat != null) {
                reservedSeats.get(theaterName).put(new_seat.getSeatName(), clientID, EXPIRING_DURATION);

                //when reserve happens before 15 sec timeout, we need to remove the old reserved seat
                if (reservedSeats.get(theaterName).get(old_seat.getSeatName()) != null) {
                    if (reservedSeats.get(theaterName).get(old_seat.getSeatName()) == clientID) {
                        reservedSeats.get(theaterName).remove(old_seat.getSeatName());
                    }

                }
                theater.freeSeat(old_seat);

                return new Message(MessageType.AVAILABLE, theater.seats, new_seat, clientID);
            }
            // if the wish seat was not free, just return the old seat
            else {
                return new Message(MessageType.AVAILABLE, theater.seats, old_seat, clientID);
            }
        }
    }

    public Message accept(String theaterName, Seat acceptedSeat, int clientID) throws RemoteException {

        //check if this client has already that seat reserved, if not, return error
        if (reservedSeats.get(theaterName).get(acceptedSeat.getSeatName()) == null ||
                reservedSeats.get(theaterName).get(acceptedSeat.getSeatName()) != clientID) {
            return new Message(MessageType.ACCEPT_ERROR);
        }

        Theater theater;
        if (dbServerLocalMode) {
            theater = (Theater) this.theaters.get(theaterName);
            if (theater.occupySeat(acceptedSeat)) {
                reservedSeats.get(theaterName).remove(acceptedSeat.getSeatName());
                return new Message(MessageType.ACCEPT_OK);
            } else {
                return new Message(MessageType.ACCEPT_ERROR);
            }
        } else {
            synchronized (reservedSeats.get(theaterName)) {
                if (dataStorageStub.occupySeat(theaterName, acceptedSeat)) {
                    reservedSeats.get(theaterName).remove(acceptedSeat.getSeatName());
                    return new Message(MessageType.ACCEPT_OK);
                } else {
                    return new Message(MessageType.ACCEPT_ERROR);
                }
            }
        }


    }

    public Message cancel(String theaterName, Seat seat, int clientID) throws RemoteException {

        //check if this client has already that seat reserved, if not, return error
        if (reservedSeats.get(theaterName).get(seat.getSeatName()) == null ||
                reservedSeats.get(theaterName).get(seat.getSeatName()) != clientID) {
            return new Message(MessageType.CANCEL_ERROR);
        } else {
            reservedSeats.get(theaterName).remove(seat.getSeatName());
            return new Message(MessageType.CANCEL_OK);
        }
    }


    @Override
    public void killServer() throws RemoteException {
        System.exit(0);
    }
}