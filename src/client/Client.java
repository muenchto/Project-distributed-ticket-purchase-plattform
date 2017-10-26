package client;

import auxiliary.*;

import java.io.Console;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;

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

        int clientID = 5;

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(5000);
            WideBoxIF wideboxStub = (WideBoxIF) registry.lookup("WideBoxServer");

            String[] theaters = wideboxStub.getNames();
            System.out.println("List of Theaters is: " + Arrays.toString(theaters));

            Console console = System.console();
            String theaterChoice = console.readLine("Please choose a theater: ");
            System.out.println("Widebox is looking for this theater: " + theaterChoice);

            Message answer = wideboxStub.query(theaterChoice);
            clientID = answer.getClientID();
            System.out.println("Hello " + clientID + ": " + theaterChoice + " has following seats: ");
            //TODO: How to the seat of the client? expand the Message?
            printSeats(answer.getTheaterSeats());

            String seatChoice = null;
            while (true){
                String choice = console.readLine("Accept (y/n)?");
                if (choice.equals("y")){
                    answer = wideboxStub.accept(clientID);
                    break;
                }
                else {
                    seatChoice = console.readLine("Choose a Seat:");
                    int seatColumn = Integer.parseInt(seatChoice.substring(1));
                    Seat seat = new Seat(Seat.SeatStatus.RESERVED, seatChoice.charAt(0), seatColumn);
                    answer = wideboxStub.reserve(seat, clientID);
                    System.out.println("Hello " + clientID + ": " + theaterChoice + " has following seats: ");
                    //TODO: How to the seat of the client? expand the Message?
                    printSeats(answer.getTheaterSeats());
                }
            }
            if (answer.getType() == MessageType.ACCEPT_OK) {
                System.out.println("You have succesfully booked the seat "+ seatChoice + " in " + theaterChoice+"!");
            }
            else {
                System.out.println("Booking the seat "+ seatChoice + " in " + theaterChoice+" was not succesfull!");
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
