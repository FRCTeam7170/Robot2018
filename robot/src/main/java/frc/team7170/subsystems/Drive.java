package frc.team7170.subsystems;

import java.util.logging.Logger;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;


public class Drive {

    private final static Logger LOGGER = Logger.getLogger(Drive.class.getName());

    static {
        LOGGER.info("Initializing drive system.");
    }

    private static WPI_TalonSRX front_left_motor = new WPI_TalonSRX(RobotMap.CAN.front_left_motor);
    private static WPI_TalonSRX front_right_motor = new WPI_TalonSRX(RobotMap.CAN.front_right_motor);
    private static WPI_TalonSRX back_left_motor = new WPI_TalonSRX(RobotMap.CAN.back_left_motor);
    private static WPI_TalonSRX back_right_motor = new WPI_TalonSRX(RobotMap.CAN.back_right_motor);

    private static SpeedControllerGroup left_motors = new SpeedControllerGroup(front_left_motor, back_left_motor);
    private static SpeedControllerGroup right_motors = new SpeedControllerGroup(front_right_motor, back_right_motor);

    private static DifferentialDrive drive = new DifferentialDrive(left_motors, right_motors);

    // These hold the X and Y speeds actually sent to the speed controllers
    public static double rob_L = 0, rob_R = 0;

    private static void smooth_current(double left, double right) {
        double dL = left - rob_L;
        double dR = right - rob_R;
        if (Math.abs(left) < RobotMap.DriveSmooth.logic_threshold_L & dL > 0 &
                (Math.abs(dL) > RobotMap.DriveSmooth.tolerance_L)) {
            rob_L += RobotMap.DriveSmooth.jump_L;
        }
        if (Math.abs(right) < RobotMap.DriveSmooth.logic_threshold_R & dR > 0 &
                (Math.abs(dR) > RobotMap.DriveSmooth.tolerance_R)) {
            rob_R += RobotMap.DriveSmooth.jump_R;
        }
    }

    public static void set_arcade(double joy_X, double joy_Y, boolean smooth) {
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

        set(left, right, smooth);
    }

    public static void set_tank(double left, double right, boolean smooth) {
        set(left, right, smooth);
    }

    private static void set(double left, double right, boolean smooth) {
        if (smooth) {
            smooth_current(left, right);
        }
        drive.tankDrive(rob_L, rob_R);
    }

    public static void brake() {
        drive.stopMotor();
    }
}
