package client;

import auxiliary.*;

import java.io.Console;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;

public class Client {



    public static void main(String[] args) {

        int clientID = 0;

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(0);
            WideBoxIF wideboxStub = (WideBoxIF) registry.lookup("WideBoxServer");

            String[] theaters = wideboxStub.getNames();
            System.out.println("List of Theaters is: " + Arrays.toString(theaters));

            Console console = System.console();
            String theaterChoice = console.readLine("Please choose a theater: ");
            System.out.println("Widebox is looking for this theater: " + theaterChoice);

            Message answer = wideboxStub.query(theaterChoice);
            clientID = answer.clientID;
            System.out.println("Hello" + clientID + ": " + theaterChoice + " has following seats: ");
            //TODO: How to the seat of the client? expand the Message?
            System.out.println(Arrays.asList(answer.theaterSeats));

            String seatChoice = console.readLine("Accept (type 'a') or choose a new seat!");
            while (!seatChoice.equals("a")) {
                //TODO: check input as a regex
                // if ()
                Seat seat = new Seat(Seat.SeatStatus.RESERVED, seatChoice.charAt(0), (int)seatChoice.charAt(1));
                answer = wideboxStub.reserve(seat, clientID);
                System.out.println("Hello" + clientID + ": " + theaterChoice + " has following seats: ");
                //TODO: How to the seat of the client? expand the Message?
                System.out.println(Arrays.asList(answer.theaterSeats));
            }
            answer = wideboxStub.accept(clientID);

            if (answer.type == MessageType.ACCEPT_OK) {
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
