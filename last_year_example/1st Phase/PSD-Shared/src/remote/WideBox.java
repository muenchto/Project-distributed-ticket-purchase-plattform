package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface WideBox extends Remote {
	public List<String> getNames() throws RemoteException;
	public String[][] getSeats(String theater) throws RemoteException;
	public State getSeatState(String name, int i, int j) throws RemoteException;
	public Message acceptSeat(String name, String user, int i, int j)throws RemoteException; //TODO maybe it can only receive user
	public Message cancelReservation(String name, String user, int i, int j) throws RemoteException; //TODO maybe it can only receive user
	public Message reserveSeat(String name, String user, int i, int j) throws RemoteException;
	public Message theaterState(String name) throws RemoteException;
	public Message initialRequest(String theaterSelect, String userId) throws RemoteException;
}
