package auxiliary;

import java.io.Serializable;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class Theater implements Serializable{
    public String theaterName;
    private int freeSeats;
    public TheaterStatus status;
    public Seat[][] seats = new Seat[26][40];

    public Theater(String theaterName) {
        this.theaterName = theaterName;
        this.freeSeats = 26*40;
        this.status = TheaterStatus.FREE;

        char row = 'A';
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 40; j++) {
                this.seats[i][j] = new Seat(Seat.SeatStatus.FREE, row, j);
            }
            row++;
        }
    }

    public Seat.SeatStatus getSeatStatus(int row, int col) {
        return seats[row-'A'][col].status;
    }

    public void freeSeat(Seat seat) {
        this.seats[seat.rowNr-'A'][seat.colNr].status = Seat.SeatStatus.FREE;
        freeSeats++;
        status = TheaterStatus.FREE;
    }

    public boolean occupySeat(Seat seat) {
        if (this.seats[seat.rowNr-'A'][seat.colNr].status == Seat.SeatStatus.FREE) {
            this.seats[seat.rowNr-'A'][seat.colNr].status = Seat.SeatStatus.OCCUPIED;
            freeSeats--;
            if (freeSeats <= 0) {
                status = TheaterStatus.FULL;
            }
            return true;
        }
        else return false;

    }
    public boolean occupySeat(int row, int col) {
        if (this.seats[row][col].status == Seat.SeatStatus.FREE) {
            this.seats[row][col].status = Seat.SeatStatus.OCCUPIED;
            freeSeats--;
            if (freeSeats <= 0) {
                status = TheaterStatus.FULL;
            }
            return true;
        }
        else return false;

    }

    public Seat reserveSeat() {
        Seat seat = null;
        for (char i = 'A'; i <= 'Z'; i++) {
            for (int j = 0; j < 40; j++) {
                if (this.seats[i-'A'][j].status == Seat.SeatStatus.FREE){
                    this.seats[i-'A'][j].status = Seat.SeatStatus.RESERVED;
                    seat = new Seat(Seat.SeatStatus.RESERVED, i, j);
                    return seat;
                }
            }
        }
        return seat;
    }

    public Seat reserveSeat(Seat seat) {
        if (this.seats[seat.rowNr - 'A'][seat.colNr].status == Seat.SeatStatus.FREE) {
            this.seats[seat.rowNr - 'A'][seat.colNr].status = Seat.SeatStatus.RESERVED;
            seat.status = Seat.SeatStatus.RESERVED;
            return seat;
        }
        else {
            return null;
        }
    }
    public void setSeatToReserved(String seatName) {

        this.seats[seatName.charAt(0)-'A'][Integer.parseInt(seatName.substring(1))].status = Seat.SeatStatus.RESERVED;

    }

   //return the number of freeSeats given from DBServer to check  if freeSeats+reservation==total seat's
    /*
    public int seatsAvailable () {
    	return freeSeats;
    }
    */

    @Override
    public Theater clone() {
        Theater clone = new Theater(this.theaterName);
        for (char i = 'A'; i <= 'Z'; i++) {
            for (int j = 0; j < 40; j++) {
                clone.seats[i-'A'][j].status = this.seats[i-'A'][j].status;
            }
        }
        return clone;
    }

    public String createStringForDB() {
        StringBuilder stringBuilder = new StringBuilder(4+26*40);
        stringBuilder.append(theaterName+":");
        for (char i = 0; i < 26; i++) {
            for (int j = 0; j < 40; j++) {
                stringBuilder.append(seats[i][j].status.ordinal());
            }
            stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

}
