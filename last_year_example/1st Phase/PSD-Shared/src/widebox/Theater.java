package widebox;

import java.rmi.Remote;

import remote.State;

public class Theater implements Remote {

	// We only consider one room and movie per theater for simplicity
	private String name;
	private Seat[][] seats;
	private String movie;
	private int id;
	private int occupied;

	public Theater(int id) {
		
		this.seats = new Seat[26][40];
		this.id = id;
		this.occupied = 0;
		for (int i = 0; i < seats.length; i++) {
			for (int j = 0; j < seats[i].length; j++) {
				String zero = "";
				if (j < 9)
					zero = "0";
				seats[i][j] = new Seat(Character.toString((char) (i + 65)) + zero + (j + 1));
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Seat[][] getSeats() {
		return seats;
	}

	public Seat getSeat(int i, int j) {
		return this.seats[i][j];
	}

	public State getSeatState(int i, int j) {
		return this.seats[i][j].getState();
	}

	public State setSeatState(State state,String user, int i, int j) {
		this.getSeat(i, j).setState(state,user);
		if (this.getSeat(i, j).getState().equals(State.OCCUPIED))
			this.occupied++;
		return this.getSeatState(i, j);
	}

	public void setSeats(Seat[][] seats) {
		this.seats = seats;
	}

	public boolean isFull() {
		return occupied == (seats.length * seats[0].length);
	}

	public String getMovie() {
		return movie;
	}

	public int getId(){
		return this.id;
	}
	
	public void setMovie(String movie) {
		this.movie = movie;
	}
	public String getFirstFreeSeat(){
		for(int i=0;i<seats.length;i++)
			for(int j=0;j<seats[i].length;j++)
				if(seats[i][j].getState().equals(State.FREE))
					return seats[i][j].getName();
		return "ERROR";
	}

}
