package frc.team7170.util;

public class CalcUtil {

    public static boolean in_threshold(double val, double median, double thresh) {
        return (median - thresh < val) & (val < median + thresh);
    }

    public static double apply_bounds(double val, double lower, double upper) {
        if (lower <= val & val <= upper) {
            return val;
        } else if (val > (upper+lower)/2) {  // If greater than mean
            return upper;
        }
        return lower;
    }
}
