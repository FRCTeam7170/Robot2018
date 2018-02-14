package frc.team7170.robot;


/**
 * The default values for all important constants used throughout the robot code.
 * Some values are marked as final (various IDs mainly) as they are assumed to never need be
 * changed while the bot is running; others--the ones controllable on the dashboard--are not.
 */
public class RobotMap {

    public static class Main {
        public static final double teleop_delay = 0.05;
    }

    public static class Debug {
        public static final boolean print_current = false;
    }

    public static class DriveCurrentSmoothing {
        public static double logic_threshold_X = 0.5;
        public static double logic_threshold_Y = 0.5;
        public static double tolerance_X = 0.1;
        public static double tolerance_Y = 0.1;
        public static double jump_X = 0.07;
        public static double jump_Y = 0.07;
    }

    public static class Camera {
        public static double speed_X = 0.02;
        public static double speed_Y = 0.02;
        public static double speed_X_45 = 0.02;
        public static double speed_Y_45 = 0.02;
    }

    public static class CAN {
        public static final int front_left_motor = 11;
        public static final int front_right_motor = 12;
        public static final int back_left_motor = 13;
        public static final int back_right_motor = 14;
        public static final int PCM = 15;
    }

    public static class PWM {
        public static final int camera_servo_X = 0;
        public static final int camera_servo_Y = 1;
    }

    public static class Controllers {
        public static final int joystick_port = 0;
    }

    public static class DIO {
        public static final int encoder_left_A = 0;
        public static final int encoder_left_B = 1;
        public static final int encoder_right_A = 2;
        public static final int encoder_right_B = 3;
    }

    public static class AIO {
        public static final int gyro = 0;
    }

    public static class RobotDims {
        // All measurements in metres (m)!
        public static final double wheel_width = 0.254; // 1 in.
        public static final double wheel_diameter = 0.1524; // 6 in.
        public static final double wheel_radius = wheel_diameter / 2.0;
        public static final double wheel_to_wheel = 0.5921; // centre to centre; 23 5/16 in.
        public static final double wheel_to_centre = wheel_to_wheel / 2.0;
        public static final double wheel_spacing = 0.2985;
        public static final double robot_width = 0.6858;
        public static final double robot_length = 0.8192;
        public static final double robot_height = 1.0986;

    }

    public static class Maneuvers {
        public static double encoder_desync_tolerance = 10.0;  // How much deviation from the expectation of lenc.get() - renc.get() is acceptable
        public static double encoder_desync_tolerance_dist = 0.05;  // metres
        public static double turn_angle_tolerance = 3.0;  // degrees
        public static double straight_distance_tolerance = 0.05;  // metres
    }
}
