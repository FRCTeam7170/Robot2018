package frc.team7170.util;

public class CalcUtil {

    public static boolean in_threshold(double val, double median, double thresh) {
        return (median - thresh < val) & (val < median + thresh);
    }
}
