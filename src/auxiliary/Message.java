package auxiliary;

import java.io.Serializable;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class Message implements Serializable{

    private MessageType type;
    private Seat[][] theaterSeats = null;
    private Seat clientsSeat = null;
    private int clientID = -1;

    public Message(MessageType messageType) {
        this.type = messageType;
    }

    public Message(MessageType messageType, Seat[][] seats, Seat clientSeat, int clientID) {
        this.type = messageType;
        this.theaterSeats = seats;
        this.clientsSeat = clientSeat;
        this.clientID = clientID;
    }

    public MessageType getType() {
        return type;
    }

    public Seat[][] getTheaterSeats() {
        return theaterSeats;
    }

    public int getClientID() {
        return clientID;
    }

    public Seat getClientsSeat() {
        return clientsSeat;
    }
}
