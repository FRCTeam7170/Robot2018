package frc.team7170.comm;


/**
 * Various speeds that a {@link Transmitter} can be polled at. This is used for {@link Transmitter#poll_rate()}.
 * Static means the transmitter is only updated once.
 * Volatile means the transmitter is updated every iteration.
 * Slow, moderate, and fast refer the 500, 250, and 100 millisecond delays between updates, respectively.
 * The default delay in {@link Transmitter} is moderate.
 */
public enum TransmitFrequency {
    STATIC              (-1),
    AFTER_REMOTE_UPDATE (-1),  // TODO, OR HAVE CALLBACK SYSTEM IN PLACE TO INDICATE SUCCESS/FAILURE IN CHANGING ENTRY VALUE
    VOLATILE            (0),
    SLOW                (500),
    MODERATE            (250),
    FAST                (100);

    int freq;

    TransmitFrequency(int ms) {
        freq = ms;
    }
}
