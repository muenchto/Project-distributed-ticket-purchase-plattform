package auxiliary;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.server.UnicastRemoteObject;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class DataStorageImpl extends UnicastRemoteObject implements DataStorageIF{
    private ConcurrentHashMap theaters;

    public DataStorageImpl(int num_theaters) throws RemoteException {

        this.theaters = new ConcurrentHashMap<String, Theater>();
        for (int i = 0; i < num_theaters; i++) {
            this.theaters.put("TheaterNr" + i, new Theater("TheaterNr" + i));
        }
        System.out.println("DBServer started with " + num_theaters + " theaters");

    }

    public String[] getTheaterNames() throws RemoteException{
        System.out.println("getNames");
        java.util.Set keys = this.theaters.keySet();
        String[] names = (String[]) keys.toArray(new String[keys.size()]);
        return names;
    }

    public Theater getTheater(String theaterName) throws RemoteException{
        System.out.println("get Theater: "+theaterName);
        return (Theater) theaters.get(theaterName);
    }

    public boolean occupySeat(String theaterName, Seat theaterSeat) throws RemoteException{
        Theater theater = (Theater) theaters.get(theaterName);
        if(theater.occupySeat(theaterSeat)) {
            System.out.println("DBServer occupied Seat "+theaterSeat.rowNr+""+theaterSeat.colNr+" in theater "+theaterName);
            return true;
        }
        else {
            System.out.println("OCCUPY ERROR: "+theaterSeat.rowNr+""+theaterSeat.colNr+" in theater "+theaterName);
            return false;
        }
    }

}
