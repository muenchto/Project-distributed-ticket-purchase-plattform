package auxiliary;

/**
 * Created by tobiasmuench on 25.10.17.
 */
public class Message implements MessageIF {

    private MessageType type;
    private Seat[][] theaterSeats = null;
    private Seat clientsSeat = null;
    private int clientID = -1;

    public Message(MessageType messageType) {
        this.type = messageType;
    }

    public Message(MessageType messageType, Seat[][] seats, int clientID) {
        this.type = messageType;
        this.theaterSeats = seats;
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
