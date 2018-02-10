package frc.team7170.robot;

public class RobotMap {

    public static class Main {
        public static final double teleop_delay = 0.05;
    }

    public static class Debug {
        public static final boolean print_current = false;
    }

    public static class DriveCurrentSmoothing {
        public static final double logic_threshold_X = 0.5;
        public static final double logic_threshold_Y = 0.5;
        public static final double tolerance_X = 0.1;
        public static final double tolerance_Y = 0.1;
        public static final double jump_X = 0.07;
        public static final double jump_Y = 0.07;
    }

    public static class Camera {
        public static final double speed_X = 0.02;
        public static final double speed_Y = 0.02;
        public static final double speed_X_45 = 0.02;
        public static final double speed_Y_45 = 0.02;
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
}
