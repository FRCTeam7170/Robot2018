package frc.team7170.subsystems.nav;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Encoder;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;


public class Navigation {

    private final static Logger LOGGER = Logger.getLogger(Drive.class.getName());

    static Encoder left_enc = new Encoder(RobotMap.DIO.encoder_left_A, RobotMap.DIO.encoder_left_B);
    static Encoder right_enc = new Encoder(RobotMap.DIO.encoder_right_A, RobotMap.DIO.encoder_right_B);

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
    public static void reset() {
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
        }
    }

    public static void add_maneuver(Maneuver man) {
        queue.add(man);
    }

    public static void add_maneuvers(Maneuver[] mans) {
        queue.addAll(Arrays.asList(mans));
    }

    static void man_complete() {
        Drive.brake();
        queue.pop();
        queue.peek().run();
    }
}
