package auxiliary;


import java.rmi.RemoteException;
import java.util.TreeSet;

/**
 * Created by tobiasmuench on 24.10.17.
 * Note to Hugo: I just set this up for test purpose.
 * I think we need this in order to be able to call the DBServer via RMI.
 * I will state here the methods that I think the DBServer has to be able to answer very soon!
 */

public interface DataStorageIF {

    public String[] getTheaterNames() throws RemoteException;

    public Theater getTheater(int theaterName) throws RemoteException;

    // make a purchase and return true if the operation was successful
    public boolean purchase(int theater, Seat seat) throws RemoteException;

    // cancel a reservation of a seat, return true if the seat was reserved
    public boolean cancelReserve(int theater, Seat seat) throws RemoteException;

    // Just in case we need it relative to reservation
    // make a random reservation within a free seat and return true if the operation was successful
    public boolean reserveSeat(int theater, Seat seat) throws RemoteException;

    // make a reserve within a specific seat and return true if the operation was successful
    public Seat reserveSeat(int theater) throws RemoteException;

    boolean occupySeat(String theaterName, Seat theaterSeat) throws RemoteException;
}
    
   
    
    

