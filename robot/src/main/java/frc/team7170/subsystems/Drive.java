package frc.team7170.subsystems;

import java.util.logging.Logger;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.team7170.robot.RobotMap;


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
    private static double rob_X = 0, rob_Y = 0;

    /**
     * Logic to control current flow to drive motors (ie prevent spikes)
     * @param XC New/current X value
     * @param YC New/current Y value
     * @param XP Old/previous X value
     * @param YP Old/previous Y value
     */
    private static double[] smooth_current(double XC, double YC, double XP, double YP) {
        double dx = Math.abs(XC) - Math.abs(XP);
        double dy = Math.abs(YC) - Math.abs(YP);
        double[] out = new double[2];
        // X -- Only activate smoothing logic if speed is below a threshold (ie prone to spikes)
        if (Math.abs(XP) < RobotMap.DriveCurrentSmoothing.logic_threshold_X & Math.abs(dx) > RobotMap.DriveCurrentSmoothing.tolerance_X) {
            out[0] = Math.signum(dx)*RobotMap.DriveCurrentSmoothing.jump_X;  // Smooth X acceleration
        } else {
            out[0] = dx;
        }
        // Y
        if (Math.abs(YP) < RobotMap.DriveCurrentSmoothing.logic_threshold_Y & Math.abs(dy) > RobotMap.DriveCurrentSmoothing.tolerance_Y) {
            out[1] = Math.signum(dy)*RobotMap.DriveCurrentSmoothing.jump_Y;  // Smooth Y acceleration
        } else {
            out[1] = dy;
        }
        return out;
    }

    /**
     * Set robot speed using arcade drive
     * @param joy_X Joystick X value
     * @param joy_Y Joystick Y value
     * @param smooth Whether to use current smoothing algorithm or not
     */
    public static void set_arcade(double joy_X, double joy_Y, boolean smooth) {
        if (smooth) {
            double[] out = smooth_current(joy_X, joy_Y, rob_X, rob_Y);
            rob_X = out[0];
            rob_Y = out[1];
        } else {
            rob_X = joy_X;
            rob_Y = joy_Y;
        }
        drive.arcadeDrive(rob_X, rob_Y);
    }

    // TODO: Do we need this?
    /**
     * Set robot speed using tank drive
     * @param left Left side wheel speed
     * @param right Right side wheel speed
     */
    public static void set_tank(double left, double right) {
        drive.tankDrive(left, right);
    }
}
