package frc.team7170.subsystems.nav;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.drive.Drive;


public class Navigation {

    private final static Logger LOGGER = Logger.getLogger(Drive.class.getName());

    private static Encoder left_enc;
    private static Encoder right_enc;

    private static BuiltInAccelerometer accelerometer;

    private static ArrayDeque<Maneuver> queue;


    public static void init() {
        LOGGER.info("Initializing navigation system.");

        left_enc = new Encoder(RobotMap.DIO.encoder_left_A, RobotMap.DIO.encoder_left_B);
        right_enc = new Encoder(RobotMap.DIO.encoder_right_A, RobotMap.DIO.encoder_right_B);
        accelerometer = new BuiltInAccelerometer();
        queue = new ArrayDeque<>();

        /*
        Let:
            r = wheel radius
            d = distance travelled
            p = #pulses
        Knowing that [ 1p = 1 degree of rotation ] and [ arc length = angle(radians) * radius ]...
        d = pi*p*r/180
         */
        left_enc.setDistancePerPulse(Math.PI * RobotMap.RobotDims.wheel_radius / 180);
        right_enc.setDistancePerPulse(Math.PI * RobotMap.RobotDims.wheel_radius / 180);
    }

    /**
     * Reset the encoder values to zero.
     */
    public static void reset_encoders() {
        LOGGER.fine("Resetting encoders.");
        left_enc.reset();
        right_enc.reset();
    }

    /**
     * Updates current maneuver. Must be called regularly from main robot loop.
     */
    public static void update() {
        Maneuver man = queue.peek();
        if (man != null) {
            if (!man.running) {
                man.run();
            }
            man.update();
            if (man.finished()) {
                LOGGER.info("Finished maneuver: " + man.toString());
                queue.pop();
            }
        }
    }

    public static void add_maneuver(Maneuver man) {
        LOGGER.info("Added maneuver: " + man.toString());
        queue.add(man);
    }

    public static void add_maneuvers(Maneuver[] mans) {
        LOGGER.info("Added maneuvers: " + Arrays.toString(mans));
        queue.addAll(Arrays.asList(mans));
    }


    // Accelerometer accessors

    public static double get_accel_X() {
        return 9.80*accelerometer.getX();  // Convert to m/s
    }

    public static double get_accel_Y() {
        return 9.80*accelerometer.getY();  // Convert to m/s
    }

    public static double get_accel_Z() {
        return 9.80*accelerometer.getZ();  // Convert to m/s
    }


    // Encoder accessors

    public static int get_Lenc() {
        return left_enc.get();
    }

    public static double get_Lenc_dist() {
        return left_enc.getDistance();
    }

    public static double get_Lenc_rate() {
        return left_enc.getRate();
    }

    public static boolean get_Lenc_dir() {
        return left_enc.getDirection();
    }

    public static boolean get_Lenc_stopped() {
        return left_enc.getStopped();
    }

    public static int get_Renc() {
        return right_enc.get();
    }

    public static double get_Renc_dist() {
        return right_enc.getDistance();
    }

    public static double get_Renc_rate() {
        return right_enc.getRate();
    }

    public static boolean get_Renc_dir() {
        return right_enc.getDirection();
    }

    public static boolean get_Renc_stopped() {
        return right_enc.getStopped();
    }
}
