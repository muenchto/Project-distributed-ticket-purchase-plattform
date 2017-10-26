package auxiliary;

import java.io.Serializable;

/**
 * Created by tobiasmuench on 25.10.17.
 */
public class Seat implements Serializable{
    public enum SeatStatus  {
        OCCUPIED,
        RESERVED,
        FREE,
    }

    public SeatStatus status;
    public char rowNr;
    public int colNr;

    public Seat(SeatStatus status, char rowNr, int colNr) {
        this.status = status;
        this.rowNr = rowNr;
        this.colNr = colNr;
    }

    public String getSeatName() {
        StringBuilder sb = new StringBuilder();
        return sb.append(rowNr).append(colNr).toString();
    }
}
