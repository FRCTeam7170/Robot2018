package frc.team7170.util;

public class CalcUtil {

    public static boolean in_threshold(double val, double median, double thresh) {
        return (median - thresh < val) & (val < median + thresh);
    }

    public static double apply_bounds(double val, double lower, double upper) {
        if (lower <= val & val <= upper) {
            return val;
        } else if (val > upper) {
            return upper;
        }
        return lower;
    }

    public static double map_range(double val, double lower, double upper, double target_lower, double target_upper) {
        return (val - lower) / (upper - lower) * (target_upper - target_lower) + target_lower;
    }
}
