package server;

import java.util.Timer;

/**
 * Created by tobiasmuench on 24.10.17.
 */
public class ClientData {
    protected int clientID;
    protected String theaterName;
    protected Seat seat;
    protected Timer timer;

    public ClientData(int clientID, String theaterName, Seat seat, Timer timer) {
        this.clientID = clientID;
        this.theaterName = theaterName;
        this.seat = seat;
        this.timer = timer;
    }
}
