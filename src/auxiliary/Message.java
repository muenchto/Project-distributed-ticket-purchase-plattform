package auxiliary;

/**
 * Created by tobiasmuench on 25.10.17.
 */
public class Message {

    public MessageType type;
    public Seat[][] theaterSeats = null;
    public int clientID = -1;

    public Message(MessageType messageType) {
        this.type = messageType;
    }

    public Message(MessageType messageType, Seat[][] seats, int clientID) {
        this.type = messageType;
        this.theaterSeats = seats;
        this.clientID = clientID;
    }
}
