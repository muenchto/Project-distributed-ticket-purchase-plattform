package auxiliary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigHandler {

	//private int clientId;
	private int numClients;
	private int numTheaters;
	private String op;
	private int rate;
	private int duration;
	
	public ConfigHandler() {
		try {
			BufferedReader bf = new BufferedReader(new FileReader(System.getProperty("user.dir")+
					"/src/client/traffic.config"));
			String line;
			String [] split;
			while((line = bf.readLine()) != null) {
				split = line.split("=");
				switch (split[0]) {
				case "numClients":
					this.numClients = Integer.parseInt(split[1]);
					break;
				case "numTheaters":
					this.numTheaters = Integer.parseInt(split[1]);
					break;
				case "op":
					this.op = split[1];
					break;
				case "rate":
					this.rate = Integer.parseInt(split[1]);
					break;
				case "duration":
					this.duration = Integer.parseInt(split[1]);
					break;
				}
			}
			bf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getNumClients() {
		return numClients;
	}

	public int getNumTheaters() {
		return numTheaters;
	}

	public String getOp() {
		return op;
	}

	public int getRate() {
		return rate;
	}

	public int getDuration() {
		return duration;
	}
}
