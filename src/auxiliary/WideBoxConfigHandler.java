package auxiliary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class WideBoxConfigHandler {

    private int expiring_time;
    private int num_theaters;
    private int num_servers;


    public WideBoxConfigHandler() {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(System.getProperty("user.dir") +
                    "/src/widebox.config"));
            String line;
            String[] split;
            while ((line = bf.readLine()) != null) {
                split = line.split(" = ");
                switch (split[0]) {
                    case "Total number of theaters":
                        this.num_theaters = Integer.parseInt(split[1]);
                        System.out.println(num_theaters);
                        break;
                    case "Total number of Database Server (equals to number of AppServers)":
                        this.num_servers = Integer.parseInt(split[1]);
                        break;
                    case "Reservation expiring time (millisec)":
                        this.expiring_time = Integer.parseInt(split[1]);
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

    public int getExpiring_time() {
        return expiring_time;
    }

    public int getNum_theaters() {
        return num_theaters;
    }

    public int getNum_servers() {
        return num_servers;
    }

    @Override
    public String toString() {
        return "WideBox is running with the following parameter:\n" +
                "Numbers of Database Server (== num AppServer) = " + num_servers+ "\n"+
                "Total number of theaters = " + num_theaters + "\n"+
                "The time until a reservation expires = " + expiring_time + "\n";
    }

}
