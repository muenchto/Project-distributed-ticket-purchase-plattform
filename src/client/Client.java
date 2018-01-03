package client;

import auxiliary.*;

import java.io.Console;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public class Client {

    private static void printSeats(Seat[][] seats) {
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 40; j++) {
                System.out.print("["+seats[i][j].rowNr+""+seats[i][j].colNr+" "+seats[i][j].status+"] ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws RemoteException {

        int clientID;

        String zkIP = "localhost";
        String zkPort = "";

        if (args.length > 0) {
            //args[0] = own IP
            System.setProperty("java.rmi.server.hostname", args[0]);

            //args[1] = zookeeper IP
            zkIP = args[1];
            //args[2] = zookeeper Port
            zkPort = args[2];
        }
        String zkAddress = zkIP + ":" + zkPort;
        final ConnectionHandler connector = new ConnectionHandler(zkAddress, null);
        final LoadBalancerIF loadBalancerStub = (LoadBalancerIF) connector.get("loadbalancer0", "/loadbalancer");


        try {

            System.out.println("Client connected to WideBox");

            HashMap<String, String[]> theaters = loadBalancerStub.getNames();
            System.out.println("List of Theaters is: " + Arrays.toString(theaters.values().toArray()));

            Scanner input = new Scanner(System.in);
            System.out.println("Please choose a theater: ");
            String theaterChoice = input.nextLine();
            int theaterNr = Integer.parseInt(theaterChoice);
            System.out.println("Widebox is looking for this theater: " + theaterNr);

            String targetAppServer = getAppServerWithTheater(theaters, theaterNr);
            WideBoxIF wideboxStub = (WideBoxIF) connector.get(targetAppServer, "/appserver");

            theaterChoice = "TheaterNr"+theaterNr;
            Message answer = wideboxStub.query(theaterChoice);
            clientID = answer.getClientID();
            System.out.println("Hello " + clientID + ": " + theaterChoice + " has following seats: ");
            printSeats(answer.getTheaterSeats());
            System.out.println("Your seat is: "+answer.getClientsSeat().rowNr+""+answer.getClientsSeat().colNr
                    +"; "+answer.getClientsSeat());

            String seatChoice = null;
            while (true){
                System.out.println("Accept (y/n) or cancel (c)?");
                String choice = input.nextLine();
                if (choice.equals("y")){
                    answer = wideboxStub.accept(theaterChoice, answer.getClientsSeat(), clientID);
                    break;
                }
                else if (choice.equals("n")){
                    System.out.println("Choose a Seat:");
                    seatChoice = input.nextLine();
                    int seatColumn = Integer.parseInt(seatChoice.substring(1));
                    Seat seat = new Seat(Seat.SeatStatus.RESERVED, seatChoice.charAt(0), seatColumn);

                    answer = wideboxStub.reserve(theaterChoice, answer.getClientsSeat(), seat, clientID);

                    System.out.println("Hello " + clientID + ": " + theaterChoice + " has following seats: ");
                    printSeats(answer.getTheaterSeats());
                    System.out.println("Your seat is: "+answer.getClientsSeat().rowNr+":"+answer.getClientsSeat().colNr
                            +"; "+answer.getClientsSeat());
                } else if (choice.equals("c")) {
                    answer = wideboxStub.cancel(theaterChoice, answer.getClientsSeat(), clientID);
                    break;
                }
            }
            System.out.println(answer.getType());
            switch (answer.getType()){
                case ACCEPT_OK:
                    System.out.println("You have successfully booked in " + theaterChoice+"!");
                    break;
                case ACCEPT_ERROR:
                    System.out.println("Booking the seat was not successful!");
                    break;
                case CANCEL_OK:
                    System.out.println("Cancel successful.");
                    break;
                case CANCEL_ERROR:
                    System.out.println("Cancel error!");
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static String getAppServerWithTheater(HashMap<String, String[]> theaters, int aux) {
        String targetTheater = "TheaterNr" + aux;
        for (Map.Entry<String, String[]> e : theaters.entrySet()) {
            for (String s : e.getValue()) {
                if (s.equals(targetTheater)) {
                    return e.getKey();
                }
            }
        }
        return null;
    }
}
