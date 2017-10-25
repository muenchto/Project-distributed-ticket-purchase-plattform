package auxiliary;

/**
 * Created by tobiasmuench on 25.10.17.
 */
public class Theater {
    public String theaterName;
    public TheaterStatus status;
    public Seat[][] seats = new Seat[26][40];

    //TODO: add logic to determine and set the theater status. Maybe via occupied/reserved counter

    public Theater(String theaterName) {
        this.theaterName = theaterName;
        this.status = TheaterStatus.FREE;

        char row = 'A';
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 40; j++) {
                this.seats[i][j] = new Seat(Seat.SeatStatus.FREE, row, j);
            }
            row++;
        }
    }

    public void freeSeat(Seat seat) {
        this.seats[seat.rowNr][seat.colNr].status = Seat.SeatStatus.FREE;
    }

    public void occupySeat(Seat seat) {
        this.seats[seat.rowNr][seat.colNr].status = Seat.SeatStatus.OCCUPIED;
    }

    public Seat reserveSeat() {
        Seat seat = null;
        //char row = 'A';
        for (char i = 'A'; i <= 'Z'; i++) {
            for (int j = 0; j < 40; j++) {
                if (this.seats[i-'A'][j].status == Seat.SeatStatus.FREE){
                    this.seats[i-'A'][j].status = Seat.SeatStatus.RESERVED;
                    seat = new Seat(Seat.SeatStatus.RESERVED, i, j);
                    return seat;
                }
            }
            //row++;
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
}
