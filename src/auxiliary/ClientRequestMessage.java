package auxiliary;

public class ClientRequestMessage implements Message {

    private String clientId;
    private String opId;
    private String operation; //what operation he wants to do, will add the ops after
    private Seat seat; //could be a seat he wants to take
    private Theater theater; //same for theater
    private Boolean confirm;

    public ClientRequestMessage(String clientId, String opId, String operation, Seat seat, Theater theater, Boolean confirm) {
        this.clientId = clientId;
        this.opId = opId;
        this.operation = operation;
        this.seat = seat;
        this.theater = theater;
        this.confirm = confirm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getOpId() {
        return opId;
    }

    public void setOpId(String opId) {
        this.opId = opId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public Theater getTheater() {
        return theater;
    }

    public void setTheater(Theater theater) {
        this.theater = theater;
    }

    public Boolean getConfirm() {
        return confirm;
    }

    public void setConfirm(Boolean confirm) {
        this.confirm = confirm;
    }
}
