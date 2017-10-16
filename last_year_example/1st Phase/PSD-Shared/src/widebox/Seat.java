package widebox;

import java.rmi.Remote;

import remote.State;

public class Seat implements Remote {
	private State state;
	private String name;
	private String user;// TODO might come in handy for locks

	public Seat(String name) {
		this.name = name;
		this.state = State.FREE; // By default created seats are FREE
		this.user="";
	}

	public State getState() {
		return this.state;
	}

	public synchronized State setState(State newState,String user) {
		// if(this.state.equals(State.FREE))//TODO reserved/occupied
		this.state = newState;
		this.user=user;
		return this.state;
	}

	public String getName() {
		return this.name;
	}
	public String getUser(){
		return this.user;
	}
}
