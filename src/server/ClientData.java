package server;

import java.util.Timer;
import auxiliary.*;

/**
 * Created by tobiasmuench on 24.10.17.
 */
public class ClientData {
    protected int clientID;
    protected String theaterName;
    protected Seat seat;

    public ClientData(int clientID, String theaterName, Seat seat) {
        this.clientID = clientID;
        this.theaterName = theaterName;
        this.seat = seat;
    }
}
