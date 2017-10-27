package auxiliary;

import java.io.Serializable;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
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
