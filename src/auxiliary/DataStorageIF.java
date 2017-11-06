package auxiliary;

/**
 * Created by tobiasmuench on 24.10.17.
 * Note to Hugo: I just set this up for test purpose.
 * I think we need this in order to be able to call the DBServer via RMI.
 * I will state here the methods that I think the DBServer has to be able to answer very soon!
 */

public interface DataStorageIF {

		String[] getTheaterNames();
		Theater getTheater(String theaterName);
		boolean occupySeat(String theaterName, Seat theaterSeat);
	}
