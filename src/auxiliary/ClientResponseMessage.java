package auxiliary;

public class ClientResponseMessage {

    private Theater [] theaters;//could also be a string[]
    private Seat suggestedSeat;
    private Boolean success;
    private String opId; //just to confirm we're dealing with the same op, not such if useful


    public ClientResponseMessage(Theater[] theaters, Boolean success, String opId) {
        this.theaters = theaters;
        this.success = success;
        this.opId = opId;
    }

    public Theater[] getTheaters() {
        return theaters;
    }

    public void setTheaters(Theater[] theaters) {
        this.theaters = theaters;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getOpId() {
        return opId;
    }

    public void setOpId(String opId) {
        this.opId = opId;
    }
}
