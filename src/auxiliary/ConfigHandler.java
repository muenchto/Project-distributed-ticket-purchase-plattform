package auxiliary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigHandler {

    private String origin;
    private String target;
    private String targetTheater;
    private int numClients;
    private int numTheaters;
    private String op;
    private int rate;
    private int duration;

    public ConfigHandler() {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(System.getProperty("user.dir") +
                    "/src/client/traffic.config"));
            String line;
            String[] split;
            boolean skip = false;
            while ((line = bf.readLine()) != null) {
                split = line.split("=");
                switch (split[0]) {
                    case "origin":
                        this.origin = split[1];
                        if (split[1].equals("single")) {
                            skip = true;
                        }
                        break;
                    case "numClients":
                        if (skip) {
                            skip = false;
                            break;
                        }
                        this.numClients = Integer.parseInt(split[1]);
                        break;
                    case "target":
                        this.target = split[1];
                        if (split[1].equals("random")) {
                            skip = true;
                        }
                        break;
                    case "targettheater":
                        if(skip){
                            skip = false;
                            break;
                        }
                        this.targetTheater = split[1];
                    case "numTheaters":
                        if (skip) {
                            skip = false;
                            break;
                        }
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

    public String getTargetTheater() {
        return targetTheater;
    }

    public String getOrigin() {
        return origin;
    }

    public String getTarget() {
        return target;
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

    @Override
    public String toString() {
        return "Traffic Generator loaded with the following values:\n" +
                "origin = " + origin+ "\n"+
                "target = " + target + "\n"+
                "targetTheater = " + targetTheater + "\n" +
                "numClients = " + numClients +"\n"+
                "numTheaters = " + numTheaters +"\n"+
                "op = " + op + "\n" +
                "rate = " + rate +"\n"+
                "duration = " + duration +"\n";
    }
}
