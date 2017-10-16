package server;

import java.util.TimerTask;

public class Timeout extends TimerTask{
	private String name;
	private String user;
	private int i;
	private int j;
	private Server serv;
	
	public Timeout(String name,String user, int i, int j , Server serv){
		this.name=name;
		this.user=user;
		this.i=i;
		this.j=j;
		this.serv=serv;
	}
	@Override
	public void run() {
		System.out.println("Operation timed out!");
		serv.cancelReservation(name, user, i, j);
		
	}

	
}
