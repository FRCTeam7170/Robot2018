package frc.team7170.robot;


/**
 * The default values for all important constants used throughout the robot code.
 * Some values are marked as final (various IDs mainly) as they are assumed to never need be
 * changed while the bot is running; others--the ones controllable on the dashboard--are not.
 */
public class RobotMap {

    public static class DriveSmooth {
        public static double logic_threshold_L = 1.0;
        public static double logic_threshold_R = 1.0;
        public static double tolerance_L = 0;
        public static double tolerance_R = 0;
        public static double jump_L = 0.000000000001;
        public static double jump_R = 0.000000000001;
    }

    public static class Drive {
        public static double rabbit_speed = 1.0;
        public static double tortoise_speed = 0.6;
    }

    public static class Camera {
        public static double speed_X = 0.02;
        public static double speed_Y = 0.02;
        public static double speed_X_45 = 0.02;
        public static double speed_Y_45 = 0.02;
        public static int resolution_w = 640;  // Max = 1280 for MS LC HD3000
        public static int resolution_h = 480;  // Max = 720 for MS LC HD3000
        public static int fps = 30;  // Max = 30 for MS LC HD3000
        public static double brightness = 0.5;  // [0, 1]
    }

    public static class CAN {
        public static final int front_left_motor = 11;
        public static final int front_right_motor = 13;
        public static final int back_left_motor = 12;
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
        public static final int extend = 7;
        public static final int retract = 4;
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
        public static final int arm_pot = 3;
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
        public static double pot_scale = -300.0;  // Max rotation of arm
        public static double pot_offset = 271.4;  // Offset off of perfectly vertical
        public static double endE_speed = 0.75;
        public static double arm_speed = 0.70;
        public static double arm_speed_multiplier = 0.50;
        public static boolean reverse_endE_left = true;
        public static boolean reverse_endE_right = false;
        public static boolean reverse_arm_left = true;
        public static boolean reverse_arm_right = true;
        public static double pot_value_kill_lower_inner = 25.0;
        public static double pot_value_kill_upper_inner = 93.0;
        public static double pot_value_kill_lower_outer = 22.0;
        public static double pot_value_kill_upper_outer = 96.0;
        public static double pot_value_base_conflict = 10.0;
        public static double pot_value_home = 0.0;     // TODO
        public static double pot_value_base = 16.0;
        public static double pot_value_switch = 0.0;   // TODO
        public static double pot_value_scale = 0.0;    // TODO
        public static double pot_value_reverse = 0.0;  // TODO
        public static double move_arm_pot_tolerance = 1.0;  // degrees
        public static double arm_analog_ignore_thresh = 0.2;  // ignore joystick input if below this value
        public static double endE_analog_ignore_thresh = 0.1;  // ignore joystick input if below this value
    }

    public static class Communication {
        public static final String DB_to_send_key = "I_SEND_THESE";
        public static final String DB_avail_keymaps = "O_AVAIL_KEYMAPS_NS";
    }
}
