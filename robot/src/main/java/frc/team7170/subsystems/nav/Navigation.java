package frc.team7170.subsystems.nav;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.AnalogGyro;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;


public class Navigation {

    private final static Logger LOGGER = Logger.getLogger(Drive.class.getName());

    static Encoder left_enc = new Encoder(RobotMap.DIO.encoder_left_A, RobotMap.DIO.encoder_left_B);
    static Encoder right_enc = new Encoder(RobotMap.DIO.encoder_right_A, RobotMap.DIO.encoder_right_B);

    private static BuiltInAccelerometer accelerometer = new BuiltInAccelerometer();
    private static AnalogGyro gyro = new AnalogGyro(RobotMap.AIO.gyro);

    private static ArrayDeque<Maneuver> queue = new ArrayDeque<>();

    static {
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
                // Log man complete
                queue.pop();
            }
        }
    }

    public static void add_maneuver(Maneuver man) {
        queue.add(man);
    }

    public static void add_maneuvers(Maneuver[] mans) {
        queue.addAll(Arrays.asList(mans));
    }

    public static double get_accel_X() {
        return 9.80*accelerometer.getX();  // Convert to m/s
    }

    public static double get_accel_Y() {
        return 9.80*accelerometer.getY();  // Convert to m/s
    }

    public static double get_accel_Z() {
        return 9.80*accelerometer.getZ();  // Convert to m/s
    }
}
