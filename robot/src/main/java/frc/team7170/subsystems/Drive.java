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

    /**
     * Logic to control current flow to drive motors (ie prevent spikes)
     * @param XC New/current X value
     * @param YC New/current Y value
     * @param XP Old/previous X value
     * @param YP Old/previous Y value
     */
    private static double[] smooth_current(double XC, double YC, double XP, double YP, boolean maintain_ratio) {
        double dx = Math.abs(XC) - Math.abs(XP);
        double dy = Math.abs(YC) - Math.abs(YP);
        // Only activate smoothing logic if speed is below a threshold (ie prone to spikes)
        boolean activate_X = Math.abs(XP) < RobotMap.DriveCurrentSmoothing.logic_threshold_X;
        boolean activate_Y = Math.abs(YP) < RobotMap.DriveCurrentSmoothing.logic_threshold_Y;
        double violation_X = Math.abs(dx) - RobotMap.DriveCurrentSmoothing.tolerance_X;
        double violation_Y = Math.abs(dy) - RobotMap.DriveCurrentSmoothing.tolerance_Y;
        double[] out = new double[2];

        if (activate_X & violation_X > 0) {
            out[0] = XP + Math.signum(dx) * RobotMap.DriveCurrentSmoothing.jump_X;  // Smooth X acceleration
            if (maintain_ratio & violation_X > violation_Y & violation_Y > 0) {  // Change the ratio according to the greatest violation
                out[1] = YP * out[0] / XP;  // Change Y by same ratio
                return out;
            }
        }
        else {  // If there are no violations, set out-speed to in-speed
            out[0] = XC;
        }
        if (activate_Y & violation_Y > 0) {
            out[1] = YP + Math.signum(dy) * RobotMap.DriveCurrentSmoothing.jump_Y;  // Smooth Y acceleration
            if (maintain_ratio) {
                out[0] = XP * out[0] / YP;  // Change X by same ratio
            }
        } else {
            out[1] = YC;
        }
        return out;
    }

    public static void set_arcade(double joy_X, double joy_Y, boolean smooth) {
        set_arcade(joy_X, joy_Y, smooth, false);
    }

    public static void set_arcade(double joy_X, double joy_Y, boolean smooth, boolean maintain_ratio) {
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

        set(left, right, smooth, maintain_ratio);
    }

    public static void set_tank(double left, double right, boolean smooth) {
        set(left, right, smooth, true);
    }

    private static void set(double left, double right, boolean smooth, boolean maintain_ratio) {
        if (smooth) {
            double[] out = smooth_current(left, right, rob_L, rob_R, maintain_ratio);
            rob_L = out[0];
            rob_R = out[1];
        } else {
            rob_L = left;
            rob_R = right;
        }
        drive.tankDrive(rob_L, rob_R);
    }

    public static void brake() {
        drive.stopMotor();
    }
}
