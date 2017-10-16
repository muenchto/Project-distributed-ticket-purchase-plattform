package db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import remote.State;

public class Database {
	//TODO maybe preserve operations in memory and write to disk only after a certain threshold
	private HashMap<Integer, String[][]> database;
	private File[] logs;
	
	public Database(){
		this.database=new HashMap<Integer, String[][]>();
		this.logs=new File[1500]; //TODO might be useful for sharding and spreading data later
		for(int i=0;i<logs.length;i++)
			logs[i]=new File("log"+i);
	}
	
	public void put(int theater, String[][]seats){
		//BufferedWriter bw=new BufferedWriter(new FileWriter(logs[theater]));
		this.database.put(theater, seats);
		//bw.write
		
	}
	public void editSeat(int theater, int i, int j, State state) throws IOException{
		String[][] seats = database.get(theater);
		// converter i para ascii,j-Estado   Character.toString((char) (i + 65))
		StringBuilder sb = new StringBuilder();
		
		sb.append(Character.toString((char) (i + 65)));
		if(j<10){
			sb.append('0');			
		}
		sb.append(j+"-");
		sb.append(state);
		seats[i][j] =sb.toString();
		BufferedWriter bw=new BufferedWriter(new FileWriter(logs[theater]));
		bw.write(sb.toString());
		bw.close();
	}
	public String[][] get(String theater){
		return this.database.get(theater);
	}
	
	
}
