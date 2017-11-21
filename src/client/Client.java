package client;

import auxiliary.*;

import java.io.Console;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

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

    public static void main(String[] args) {

        int clientID;

        System.setProperty( "java.rmi.server.hostname", "192.168.RMIServer.IP" ) ;

        try {
            Registry reg;
            if (args.length > 0) {
                reg = LocateRegistry.getRegistry(args[0],5001);
            }
            else {
                reg = LocateRegistry.getRegistry(5001);
            }
            WideBoxIF wideboxStub = (WideBoxIF) reg.lookup("WideBoxServer");
            System.out.println("Client connected to WideBoxServer");

            String[] theaters = wideboxStub.getNames();
            System.out.println("List of Theaters is: " + Arrays.toString(theaters));

            Console console = System.console();
            String theaterChoice = console.readLine("Please choose a theater: ");
            System.out.println("Widebox is looking for this theater: " + theaterChoice);

            Message answer = wideboxStub.query(theaterChoice);
            clientID = answer.getClientID();
            System.out.println("Hello " + clientID + ": " + theaterChoice + " has following seats: ");
            printSeats(answer.getTheaterSeats());
            System.out.println("Your seat is: "+answer.getClientsSeat().rowNr+""+answer.getClientsSeat().colNr
                    +"; "+answer.getClientsSeat());

            String seatChoice = null;
            while (true){
                String choice = console.readLine("Accept (y/n) or cancel (c)?");
                if (choice.equals("y")){
                    answer = wideboxStub.accept(theaterChoice, answer.getClientsSeat(), clientID);
                    break;
                }
                else if (choice.equals("n")){
                    seatChoice = console.readLine("Choose a Seat:");
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
}
