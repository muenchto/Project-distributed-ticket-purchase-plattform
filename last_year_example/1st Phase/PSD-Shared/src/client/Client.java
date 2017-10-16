package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import remote.Code;
import remote.Message;
import remote.WideBox;

public class Client { // TODO soon to be traffic generator?
	public static final int SERVER_PORT = 5000;// default value for a server
												// port

	public static void main(String[] args) throws Exception {
		String serverHost = (args.length < 1) ? null : args[0];
		Scanner scan = new Scanner(System.in);
		Random rand = new Random();
		String theaterSelect, seatColumn;
		char seatRow;
		String userId;
		int requests = 0;
		int accepted = 0;
		int canceled = 0;
		int reserves = 0;
		int busy = 0;

		try {

			System.out.println(serverHost);
			Registry registry = LocateRegistry.getRegistry(serverHost, SERVER_PORT);
			WideBox test = (WideBox) registry.lookup("WideBox");
			// Some extra interface

			System.out.println("Starting Traffic Generator");
			System.out.println("1-Manual Mode | 2-Auto Mode");
			String option = scan.nextLine();
			switch (Integer.parseInt(option)) {
			case 1:
				System.out.println("Please insert Client ID");
				userId = scan.nextLine();
				System.out.println("Welcome to Widebox here is a list of our available theaters");
				printTheaters(test);
				System.out.print("\nPlease choose a theater to get the list of seats:");
				theaterSelect = scan.nextLine();
				Message msg = test.initialRequest(theaterSelect, userId);
				while (msg.getCode().equals(remote.Code.FULL)) {
					System.out.println("Theater is full please choose another!");
					theaterSelect = scan.nextLine();
					msg = test.initialRequest(theaterSelect, userId);
				}
				printMsgSeats(msg);
				System.out.print("We have provisionally reserved a seat for you, do you want this one?");
				System.out.println(msg.getSuggestedName());
				break;
			// TODO Complete
			case 2:

				System.out.println("What would you like to do?");
				System.out.print("1-Query Only | 2- Query and Accept");
				String mode;
				mode = scan.nextLine();

				userId = "Client n " + rand.nextInt(99999);
				int i = 0;

				switch (Integer.parseInt(mode)) {
				case 1:
					while (true) {
						Message reply = test.initialRequest("Theater n" + i, userId);
						requests++;
						if (reply.getCode().equals(Code.BUSY))
							busy++;
						if (reply.getCode().equals(Code.AVAILABLE))
							reserves++;
						if (reply.getCode().equals(Code.FULL))
							i++;
					}
				case 2:
					while (true) {//Queries and Accepts every single suggestion
						Message reply = test.initialRequest("Theater n" + i, userId);
						requests++;
						if (reply.getCode().equals(Code.BUSY))
							busy++;
						if (reply.getCode().equals(Code.RESERVE_OK)) {
							reserves++;
							String seat = reply.getSuggestedName();
							System.out.println("Rec'd Seat is "+reply.getSuggestedName());
							test.acceptSeat("Theater n" + i, userId, getSeatRow(seat), getSeatColumn(seat)-1);
							System.out.println("accepted seat: " + seat + " at Theater n" + i);
						}
						if (reply.getCode().equals(Code.FULL))
							i++;
					}
				}

			}

		} catch (

		Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
			scan.close();
		}

	}

	private static void printSeats(WideBox widebox, String name) throws RemoteException {
		String[][] seats = widebox.getSeats(name);
		System.out.println("\nSeats:");
		for (int i = 0; i < seats.length; i++) {
			for (int j = 0; j < seats[i].length; j++) {
				System.out.print(seats[i][j] + " ");
			}
			System.out.println();
		}
	}

	private static void printTheaters(WideBox widebox) throws RemoteException {
		List<String> theaters = widebox.getNames();
		for (String s : theaters)
			System.out.println(s);
	}

	private static void printMsgSeats(Message msg) {
		String[][] seats = msg.getSeats();
		for (int i = 0; i < seats.length; i++) {
			for (int j = 0; j < seats[i].length; j++) {
				System.out.print(seats[i][j] + " ");
			}
			System.out.println();
		}
	}

	private static int getSeatRow(String name) {
		String[] seat = name.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		char pos = seat[0].charAt(0);
		return (int) pos - 65;
	}

	private static int getSeatColumn(String name) {
		String[] seat = name.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		return Integer.parseInt(seat[1]);
	}

}
