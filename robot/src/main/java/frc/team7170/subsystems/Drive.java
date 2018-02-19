package frc.team7170.subsystems;

import java.util.logging.Logger;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;


public class Drive {

    private final static Logger LOGGER = Logger.getLogger(Drive.class.getName());

    private static WPI_TalonSRX front_left_motor;
    private static WPI_TalonSRX front_right_motor;
    private static WPI_TalonSRX back_left_motor;
    private static WPI_TalonSRX back_right_motor;

    private static SpeedControllerGroup left_motors;
    private static SpeedControllerGroup right_motors;

    private static DifferentialDrive drive;

    // These hold the L and R speeds actually sent to the speed controllers
    private static double rob_L = 0, rob_R = 0;

    public static void init() {
        LOGGER.info("Initializing drive system.");

        front_left_motor = new WPI_TalonSRX(RobotMap.CAN.front_left_motor);
        front_right_motor = new WPI_TalonSRX(RobotMap.CAN.front_right_motor);
        back_left_motor = new WPI_TalonSRX(RobotMap.CAN.back_left_motor);
        back_right_motor = new WPI_TalonSRX(RobotMap.CAN.back_right_motor);

        left_motors = new SpeedControllerGroup(front_left_motor, back_left_motor);
        right_motors = new SpeedControllerGroup(front_right_motor, back_right_motor);

        drive = new DifferentialDrive(left_motors, right_motors);
    }

    private static void smooth_current(double left, double right) {
        double dL = left - rob_L;
        double dR = right - rob_R;
        if (Math.abs(left) < RobotMap.DriveSmooth.logic_threshold_L & dL > 0 &
                dL > RobotMap.DriveSmooth.tolerance_L) {
            rob_L += RobotMap.DriveSmooth.jump_L;
        } else {
            rob_L = left;
        }
        if (Math.abs(right) < RobotMap.DriveSmooth.logic_threshold_R & dR > 0 &
                dR > RobotMap.DriveSmooth.tolerance_R) {
            rob_R += RobotMap.DriveSmooth.jump_R;
        } else {
            rob_R = right;
        }
    }

    public static void set_arcade(double joy_X, double joy_Y, boolean smooth, boolean square) {
        joy_X = CalcUtil.apply_bounds(joy_X, -1.0, 1.0);
        joy_Y = CalcUtil.apply_bounds(joy_Y, -1.0, 1.0);

        // This is copied from the setArcade function in DifferentialDrive
        double left;
        double right;

        double maxInput = Math.copySign(Math.max(Math.abs(joy_X), Math.abs(joy_Y)), joy_X);

        if (joy_X >= 0.0) {
            // First quadrant, else second quadrant
            if (joy_Y >= 0.0) {
                left = maxInput;
                right = joy_X - joy_Y;
            } else {
                left = joy_X + joy_Y;
                right = maxInput;
            }
        } else {
            // Third quadrant, else fourth quadrant
            if (joy_Y >= 0.0) {
                left = joy_X + joy_Y;
                right = maxInput;
            } else {
                left = maxInput;
                right = joy_X - joy_Y;
            }
        }

        set_tank(left, right, smooth, square);
    }

    public static void set_tank(double left, double right, boolean smooth, boolean square) {
        if (smooth) {
            smooth_current(left, right);
        } else {
            rob_L = left;
            rob_R = right;
        }
        drive.tankDrive(rob_L, rob_R, square);
    }

    public static void brake() {
        drive.stopMotor();
    }

    public static double get_L() {
        return rob_L;
    }

    public static double get_R() {
        return rob_R;
    }

    // TODO: Accessors for CAN data on motors, ex: current output
}
