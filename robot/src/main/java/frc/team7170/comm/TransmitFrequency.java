package frc.team7170.comm;


public enum TransmitFrequency {
    STATIC   (-1),
    VOLATILE (0),
    SLOW     (500),
    MODERATE (250),
    FAST     (100);

    int freq;

    TransmitFrequency(int ms) {
        freq = ms;
    }
}
