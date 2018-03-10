package frc.team7170.subsystems.drive;

import java.util.logging.Logger;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;
import frc.team7170.jobs.Dispatcher;


/**
 * Module to handle drive base motors, navigational sensors, and all interactions with them.
 */
public class Drive extends Module {

    private final static Logger LOGGER = Logger.getLogger(Drive.class.getName());

    private static Drive instance = new Drive();  // Singleton
    public static Drive get_instance() {
        return instance;
    }
    private Drive() {
        LOGGER.info("Initializing drive system.");

        front_left_motor = new WPI_TalonSRX(RobotMap.CAN.front_left_motor);
        front_right_motor = new WPI_TalonSRX(RobotMap.CAN.front_right_motor);
        back_left_motor = new WPI_TalonSRX(RobotMap.CAN.back_left_motor);
        back_right_motor = new WPI_TalonSRX(RobotMap.CAN.back_right_motor);

        left_motors = new SpeedControllerGroup(front_left_motor, back_left_motor);
        right_motors = new SpeedControllerGroup(front_right_motor, back_right_motor);

        drive = new DifferentialDrive(left_motors, right_motors);

        left_enc = new Encoder(RobotMap.DIO.encoder_left_A, RobotMap.DIO.encoder_left_B);
        right_enc = new Encoder(RobotMap.DIO.encoder_right_A, RobotMap.DIO.encoder_right_B);
        accelerometer = new BuiltInAccelerometer();

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

        Dispatcher.get_instance().register_module(this);
    }

    private WPI_TalonSRX front_left_motor;
    private WPI_TalonSRX front_right_motor;
    private WPI_TalonSRX back_left_motor;
    private WPI_TalonSRX back_right_motor;

    private SpeedControllerGroup left_motors;
    private SpeedControllerGroup right_motors;

    private DifferentialDrive drive;

    private Encoder left_enc;
    private Encoder right_enc;

    private BuiltInAccelerometer accelerometer;

    // These hold the L and R speeds actually sent to the speed controllers
    private double rob_L = 0, rob_R = 0;

    @Override
    protected void update() {}

    @Override
    protected void enabled() {}

    @Override
    protected void disabled() {
        brake();
    }

    @Override
    public String toString() {
        return "Drive module.";
    }

    /**
     * Optional algorithm to reduce jerk (change in acceleration) during teleop manual control period. This method
     * alters the rob_L and rob_R motor speeds in-place.
     * @param left Left motor speed on joystick.
     * @param right RIght motor speed on joystick.
     */
    private void smooth_current(double left, double right) {
        double dL = left - rob_L;
        double dR = right - rob_R;
        if (Math.abs(left) < RobotMap.DriveSmooth.logic_threshold_L && dL > 0 &&
                dL > RobotMap.DriveSmooth.tolerance_L) {
            rob_L += RobotMap.DriveSmooth.jump_L;
        } else {
            rob_L = left;
        }
        if (Math.abs(right) < RobotMap.DriveSmooth.logic_threshold_R && dR > 0 &&
                dR > RobotMap.DriveSmooth.tolerance_R) {
            rob_R += RobotMap.DriveSmooth.jump_R;
        } else {
            rob_R = right;
        }
    }

    /**
     * Set the motors using an arcade joystick style. Note that this is literally a copy-paste of the arcade algorithm
     * used in {@link DifferentialDrive#arcadeDrive(double, double, boolean)} so that we can keep all interactions with
     * the motors in a consistent tank-drive system while still offering the ability to control the robot via arcade
     * drive.
     * @param joy_Y Joystick Y axis.
     * @param joy_Z Joystick Z (turn) axis.
     * @param smooth Whether to use {@link Drive#smooth_current(double, double)}.
     * @param square Whether to square the motor outputs to allow fine control at low speeds.
     */
    public void set_arcade(double joy_Y, double joy_Z, boolean smooth, boolean square) {
        joy_Y = CalcUtil.apply_bounds(joy_Y, -1.0, 1.0);
        joy_Z = CalcUtil.apply_bounds(joy_Z, -1.0, 1.0);

        // The following is copied from the setArcade function in DifferentialDrive
        double left;
        double right;

        double maxInput = Math.copySign(Math.max(Math.abs(joy_Y), Math.abs(joy_Z)), joy_Y);

        if (joy_Y >= 0.0) {
            // First quadrant, else second quadrant
            if (joy_Z >= 0.0) {
                left = maxInput;
                right = joy_Y - joy_Z;
            } else {
                left = joy_Y + joy_Z;
                right = maxInput;
            }
        } else {
            // Third quadrant, else fourth quadrant
            if (joy_Z >= 0.0) {
                left = joy_Y + joy_Z;
                right = maxInput;
            } else {
                left = maxInput;
                right = joy_Y - joy_Z;
            }
        }

        set_tank(left, right, smooth, square);
    }

    /**
     * Set the motors using a tank drive style.
     * @param left Left motor speed.
     * @param right Right motor speed.
     * @param smooth Whether to use {@link Drive#smooth_current(double, double)}.
     * @param square Whether to square the motor outputs to allow fine control at low speeds.
     */
    public void set_tank(double left, double right, boolean smooth, boolean square) {
        if (smooth) {
            smooth_current(left, right);
        } else {
            rob_L = left;
            rob_R = right;
        }
        drive.tankDrive(rob_L, rob_R, square);
    }

    /**
     * Stop the motors.
     */
    public void brake() {
        drive.stopMotor();
    }

    /**
     * @return Left motor speed.
     */
    public double get_L() {
        return rob_L;
    }

    /**
     * @return Right motor speed.
     */
    public double get_R() {
        return rob_R;
    }

    /**
     * @return Y motor speed via conversion of left and right motor speeds.
     */
    public double get_Y() {
        return rob_R + (rob_L - rob_R)/2;
    }

    /**
     * @return Z motor speed via conversion of left and right motor speeds.
     */
    public double get_Z() {
        return (rob_L - rob_R)/2;
    }

    /**
     * Reset the encoder values to zero.
     */
    public void reset_encoders() {
        LOGGER.fine("Resetting encoders.");
        left_enc.reset();
        right_enc.reset();
    }


    // Accelerometer accessors

    public double get_accel_X() {
        return 9.80*accelerometer.getX();  // Convert to m/s^2
    }

    public double get_accel_Y() {
        return 9.80*accelerometer.getY();  // Convert to m/s^2
    }

    public double get_accel_Z() {
        return 9.80*accelerometer.getZ();  // Convert to m/s^2
    }


    // Encoder accessors

    public int get_Lenc() {
        return left_enc.get();
    }

    public double get_Lenc_dist() {
        return left_enc.getDistance();
    }

    public double get_Lenc_rate() {
        return left_enc.getRate();
    }

    public boolean get_Lenc_dir() {
        return left_enc.getDirection();
    }

    public boolean get_Lenc_stopped() {
        return left_enc.getStopped();
    }

    public int get_Renc() {
        return right_enc.get();
    }

    public double get_Renc_dist() {
        return right_enc.getDistance();
    }

    public double get_Renc_rate() {
        return right_enc.getRate();
    }

    public boolean get_Renc_dir() {
        return right_enc.getDirection();
    }

    public boolean get_Renc_stopped() {
        return right_enc.getStopped();
    }

    // TODO: Accessors for CAN data on motors, ex: current output
}
