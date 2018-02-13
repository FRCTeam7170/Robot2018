package frc.team7170.subsystems.nav;

import java.util.ArrayDeque;
import java.util.logging.Logger;
import edu.wpi.first.wpilibj.Encoder;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Drive;


public class Navigation {

    private final static Logger LOGGER = Logger.getLogger(Drive.class.getName());

    static Encoder left_enc = new Encoder(RobotMap.DIO.encoder_left_A, RobotMap.DIO.encoder_left_B);
    static Encoder right_enc = new Encoder(RobotMap.DIO.encoder_right_A, RobotMap.DIO.encoder_right_B);

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

    }

    /**
     * Turn on the spot at a given speed.
     * @param speed Speed to execute turn at in range [-1, 1]. Positive is clockwise.
     */
    public static void turn(double speed) {
        Drive.set_arcade(speed, 0, false);
    }

    /**
     * Turn on the spot at a given speed to a given angle
     * @param degrees Angle in degrees to turn to in [0, 360].
     * @param speed Speed to execute turn at in range [-1, 1]. Positive is clockwise.
     */
    public static void turn_degrees(double degrees, double speed) {

    }

    public static void drive(double speed) {
        // TODO
    }

    public static void arc() {

    }
}
