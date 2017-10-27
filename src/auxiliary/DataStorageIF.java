package auxiliary;

/**
 * PSD Project - Phase 1
 * @author group: psd002 ; members: 42560-50586-30360
 */
public interface DataStorageIF {

    String[] getTheaterNames();
    Theater getTheater(String theaterName);
    boolean occupySeat(String theaterName, Seat theaterSeat);
}
