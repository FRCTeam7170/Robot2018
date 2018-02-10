package frc.team7170.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.team7170.robot.RobotMap;


public class Drive {

    private static WPI_TalonSRX front_left_motor = new WPI_TalonSRX(RobotMap.CAN.front_left_motor);
    private static WPI_TalonSRX front_right_motor = new WPI_TalonSRX(RobotMap.CAN.front_right_motor);
    private static WPI_TalonSRX back_left_motor = new WPI_TalonSRX(RobotMap.CAN.back_left_motor);
    private static WPI_TalonSRX back_right_motor = new WPI_TalonSRX(RobotMap.CAN.back_right_motor);

    private static SpeedControllerGroup left_motors = new SpeedControllerGroup(front_left_motor, back_left_motor);
    private static SpeedControllerGroup right_motors = new SpeedControllerGroup(front_right_motor, back_right_motor);

    private static DifferentialDrive drive = new DifferentialDrive(left_motors, right_motors);

    // These hold the X and Y speeds actually sent to the speed controllers
    public static double rob_X = 0, rob_Y = 0;

    /**
     * Logic to control current flow to drive motors (ie prevent spikes)
     * @param joy_X Joystick X value
     * @param joy_Y Joystick Y value
     */
    private static void smooth_current(double joy_X, double joy_Y) {
        double dx = Math.abs(joy_X) - Math.abs(rob_X);
        double dy = Math.abs(joy_Y) - Math.abs(rob_Y);
        // X -- Only activate smoothing logic if speed is below a threshold (ie prone to spikes)
        if (Math.abs(rob_X) < RobotMap.DriveCurrentSmoothing.logic_threshold_X & Math.abs(dx) > RobotMap.DriveCurrentSmoothing.tolerance_X) {
            rob_X += Math.signum(dx)*RobotMap.DriveCurrentSmoothing.jump_X;  // Smooth X acceleration
        } else {
            rob_X += dx;
        }

        // Y
        if (Math.abs(rob_Y) < RobotMap.DriveCurrentSmoothing.logic_threshold_Y & Math.abs(dy) > RobotMap.DriveCurrentSmoothing.tolerance_Y) {
            rob_Y += Math.signum(dy)*RobotMap.DriveCurrentSmoothing.jump_Y;  // Smooth Y acceleration
        } else {
            rob_Y += dy;
        }
    }

    /**
     * Set robot speed using arcade drive
     * @param joy_X Joystick X value
     * @param joy_Y Joystick Y value
     * @param smooth Whether to use current smoothing algorithm or not
     */
    public static void set(double joy_X, double joy_Y, boolean smooth) {
        if (smooth) {
            smooth_current(joy_X, joy_Y);
        } else {
            rob_X = joy_X;
            rob_Y = joy_Y;
        }
        drive.arcadeDrive(rob_X, rob_Y);
    }
}
