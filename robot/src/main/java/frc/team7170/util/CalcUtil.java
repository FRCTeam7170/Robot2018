package frc.team7170.util;


/**
 * A collection of various calculations that might be done frequently enough throughout the robot code as to warrant the
 * need for a unified location for all of them.
 */
public class CalcUtil {

    /**
     * Check if a number lies within some threshold of some intermediary value.
     * @param val The number.
     * @param median The number the threshold is centered on.
     * @param thresh The threshold.
     * @return If the number lies within the threshold.
     */
    public static boolean in_threshold(double val, double median, double thresh) {
        return (median - thresh < val) && (val < median + thresh);
    }

    /**
     * Limit a given value to be within a certain domain.
     * @param val The value to check.
     * @param lower The lower bound.
     * @param upper The upper bound.
     * @return the original value if lower <= val <= upper else the extreme of the range which the value exceeds.
     */
    public static double apply_bounds(double val, double lower, double upper) {
        if (lower <= val && val <= upper) {
            return val;
        } else if (val > upper) {
            return upper;
        }
        return lower;
    }

    /**
     * Convert a number originally defined within a given range and alter it to fit a new range in such a way that the
     * ratio between the number's difference to its upper and lower bounds remains constant before and after the mapping.
     * @param val The number.
     * @param lower The original lower bound.
     * @param upper The original upper bound.
     * @param target_lower The new lower bound.
     * @param target_upper The new upper bound.
     * @return The new mapped value.
     */
    public static double map_range(double val, double lower, double upper, double target_lower, double target_upper) {
        return (val - lower) / (upper - lower) * (target_upper - target_lower) + target_lower;
    }
}
