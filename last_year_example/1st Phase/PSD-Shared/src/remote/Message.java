package remote;

import java.io.Serializable;
import java.rmi.RemoteException;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private Code code;
	private String[] theaters;
	private String[][] seats;
	private String suggestedSeat;
	
	public Message(Code code, String[] theaters){
		this.code=code;
		this.theaters=theaters;
	}
	public Message(Code code, String[][] seats){
		this.code=code;
		this.seats=seats;
	}
	
	public Message(Code code, String[][] seats, String suggestedSeat){
		this.code=code;
		this.seats=seats;
		this.suggestedSeat=suggestedSeat;
	}
	public Message(Code code){
		this.code=code;
	}
	public Code getCode() {
		return code;
	}
	public String[] getTheaters() {
		return theaters;
	}
	public String[][] getSeats() {
		return seats;
	}
	public String getSuggestedName(){
		return suggestedSeat;
	}
}
