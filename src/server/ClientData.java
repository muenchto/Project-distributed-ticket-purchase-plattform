package server;

import auxiliary.Seat;

/**
 * PSD Project - Phase 1
 *
 * @author group: psd002 ; members: 42560-50586-30360
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
