package frc.team7170.robot;


/**
 * The default values for all important constants used throughout the robot code.
 * Some values are marked as final (various IDs mainly) as they are assumed to never need be
 * changed while the bot is running; others--the ones controllable on the dashboard--are not.
 */
public class RobotMap {

    public static class Debug {
        public static final boolean print_current = false;
    }

    public static class DriveSmooth {
        public static double logic_threshold_L = 0.5;
        public static double logic_threshold_R = 0.5;
        public static double tolerance_L = 0.1;
        public static double tolerance_R = 0.1;
        public static double jump_L = 0.07;
        public static double jump_R = 0.07;
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
        public static final int arm_left_motor = 0;
        public static final int arm_right_motor = 1;
        public static final int endE_left_motor = 2;
        public static final int endE_right_motor = 3;
        public static final int camera_servo_X = 4;
        public static final int camera_servo_Y = 5;
    }

    public static class PCM {
        public static final int left_solenoid = 0;
        public static final int right_solenoid = 1;
    }

    public static class Controllers {
        public static final int joystick = 0;
        public static final int gamepad = 1;
    }

    public static class DIO {
        public static final int encoder_left_A = 0;
        public static final int encoder_left_B = 1;
        public static final int encoder_right_A = 2;
        public static final int encoder_right_B = 3;
    }

    public static class AIO {
        public static final int arm_pot = 0;
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
        public static double turn_angle_tolerance = 1.0;  // degrees
        public static double straight_distance_tolerance = 0.05;  // metres
    }

    public static class Arm {
        public static double pot_scale = 1;
        public static double pot_offset = 0;
        public static double endE_speed = 0.75;  // [0, 1]
        public static double arm_speed = 1;
        public static boolean reverse_endE_left = true;
        public static boolean reverse_endE_right = false;
        public static double pot_value_kill_lower_inner = 0;
        public static double pot_value_kill_upper_inner = 0;
        public static double pot_value_kill_lower_outer = 0;
        public static double pot_value_kill_upper_outer = 0;
        public static double pot_value_home = 0;
        public static double pot_value_base = 0;
        public static double pot_value_switch = 0;
        public static double pot_value_scale = 0;
        public static double pot_value_reverse = 0;
    }
}
