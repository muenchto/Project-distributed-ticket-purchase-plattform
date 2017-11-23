package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import remote.Code;
import remote.Seat;
import remote.State;
import remote.WideBox;
import widebox.Theater;

public class Server implements WideBox {
	private LinkedHashMap<String, Theater> theaters; // TODO maybe change to
														// LinkedHashMap
	private static final int PORT = 5000;

	public Server() {
		this.theaters = new LinkedHashMap<String, Theater>();
		for (int i = 0; i < 1500; i++) {
			this.theaters.put("Theater n" + i, new Theater(i));// note that
																// names start
																// at 0
		}
	}

	// RMI Methods

	public List<String> getNames() {
		List<String> keys = new ArrayList<String>();

		for (Map.Entry<String, Theater> t : theaters.entrySet()) {
			keys.add(t.getKey());
		}
		return keys;
	}

	// TODO Method to return list
	@Override
	public State getSeatState(String name, int i, int j) {
		Theater t = theaters.get(name);
		return t.getSeatState(i, j);

	}

	// TODO think about occupation and reservation
	public remote.Message acceptSeat(String name, String user, int i, int j) {
		Theater t = theaters.get(name);
		if (t.getSeatState(i, j).equals(State.RESERVED)) {
			t.setSeatState(State.OCCUPIED, user, i, j);
			return new remote.Message(remote.Code.ACCEPT_OK, getSeats(name)); // TODO
																				// NOT
																				// SURE
		}
		return new remote.Message(remote.Code.ACCEPT_ERROR, getSeats(name));
	}

	public remote.Message cancelReservation(String name, String user, int i, int j) {
		Theater t = theaters.get(name);
		if (t.getSeatState(i, j).equals(State.RESERVED)) {
			t.setSeatState(State.FREE, user, i, j);
			return new remote.Message(remote.Code.CANCEL_OK, getSeats(name)); // TODO
																				// NOT
																				// SURE
		}
		return new remote.Message(remote.Code.CANCEL_ERROR, getSeats(name));
	}

	
	public remote.Message initialRequest(String name, String user){
		Theater t = theaters.get(name);
		String suggestion = suggestionReserve(name);
		if(suggestion.equals("ERROR"))
			return new remote.Message(Code.FULL);
		String[] seat = suggestion.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		char pos = seat[0].charAt(0);
		if(t.isFull())
			return theaterState(name);
		return reserveSeat(name, user, (int)(pos)-65,Integer.valueOf(seat[1])-1);
	}
	
	// TODO maybe change to [][]
	public String[][] getSeats(String name) {
		String[][] string = new String[26][40];
		Theater t = theaters.get(name);
		for (int i = 0; i < t.getSeats().length; i++)
			for (int j = 0; j < t.getSeats()[i].length; j++)
				string[i][j] = (t.getSeat(i, j).getName() + "-" + t.getSeatState(i, j));
		return string;
	}

	public remote.Message reserveSeat(String name, String user, int i, int j) {
		Timer timer = new Timer();
		Timeout timeout = new Timeout(name, user, i, j, this);
		Theater t = theaters.get(name);
		if (t.getSeatState(i, j).equals(State.FREE)) {
			t.setSeatState(State.RESERVED, user, i, j);
			timer.schedule(timeout, 15000);
			return new remote.Message(remote.Code.RESERVE_OK, getSeats(name), t.getSeat(i, j).getName()); // TODO
																				// NOT
																				// SURE
		}
		return new remote.Message(remote.Code.RESERVE_ERROR, getSeats(name), t.getSeat(i, j).getName());
	}

	public remote.Message theaterState(String name) {
		if (theaters.get(name).isFull())
			return new remote.Message(remote.Code.FULL);
		else
			return new remote.Message(remote.Code.AVAILABLE, getSeats(name));
	}

	private String suggestionReserve(String name){
		Theater t=theaters.get(name);
		return t.getFirstFreeSeat();
	}
	private remote.Message busy(){
		return new remote.Message(remote.Code.BUSY);
	}
	
	public static void main(String[] args) throws Exception {
		// Implement structure
		try {
			Server serv = new Server();
			WideBox widebox = (WideBox) UnicastRemoteObject.exportObject(serv, 0);
			// String address = System.getProperty("java.rmi.server.hostname");
			// String myID = ("address" +":"+"WideBox");
			Registry registry = LocateRegistry.createRegistry(PORT);
			registry.bind("WideBox", widebox);
			System.err.println("AppServer Ready");
		} catch (Exception e) {
			System.err.print("AppServer Exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
