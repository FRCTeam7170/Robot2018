package frc.team7170.util;


public class DebugUtil {
    public static void assert_(boolean test, String msg) {
        if (!test) {
            throw new AssertionError(msg);
        }
    }
}
