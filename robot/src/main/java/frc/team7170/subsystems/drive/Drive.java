package frc.team7170.subsystems.drive;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.RpcAnswer;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.team7170.comm.*;
import frc.team7170.control.Action;
import frc.team7170.control.Control;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;

import java.util.logging.Logger;


/**
 * Module to handle drive base motors, navigational sensors, and all interactions with them.
 */
public class Drive extends Module implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(Drive.class.getName());

    private static Drive instance = new Drive();  // Singleton
    public static Drive get_instance() {
        return instance;
    }
    private Drive() {
        LOGGER.info("Initializing drive system.");

        /*
        Explanation of the calculation in the encoder's distance scaling below:
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
        register_comm();
    }

    private WPI_TalonSRX front_left_motor = new WPI_TalonSRX(RobotMap.CAN.front_left_motor);
    private WPI_TalonSRX front_right_motor = new WPI_TalonSRX(RobotMap.CAN.front_right_motor);
    private WPI_TalonSRX back_left_motor = new WPI_TalonSRX(RobotMap.CAN.back_left_motor);
    private WPI_TalonSRX back_right_motor = new WPI_TalonSRX(RobotMap.CAN.back_right_motor);

    private SpeedControllerGroup left_motors = new SpeedControllerGroup(front_left_motor, back_left_motor);
    private SpeedControllerGroup right_motors = new SpeedControllerGroup(front_right_motor, back_right_motor);

    private DifferentialDrive drive = new DifferentialDrive(left_motors, right_motors);

    private Encoder left_enc = new Encoder(RobotMap.DIO.encoder_left_A, RobotMap.DIO.encoder_left_B);
    private Encoder right_enc = new Encoder(RobotMap.DIO.encoder_right_A, RobotMap.DIO.encoder_right_B);

    private BuiltInAccelerometer accelerometer = new BuiltInAccelerometer();

    // These hold the L and R speeds actually sent to the speed controllers
    private double rob_L = 0, rob_R = 0;

    @Override
    protected void update() {
    }

    @Override
    protected void enabled() {
        LOGGER.info("Drive enabled.");
    }

    @Override
    protected void disabled() {
        LOGGER.info("Drive disabled.");
        rob_L = 0;
        rob_R = 0;
        brake();
    }

    @Override
    public String toString() {
        return "Drive System";
    }

    /**
     * Optional algorithm to reduce jerk (change in acceleration) during teleop manual control period. This method
     * alters the rob_L and rob_R motor speeds in-place.
     * @param left Left motor speed on joystick.
     * @param right RIght motor speed on joystick.
     */
    private void smooth_current(double left, double right) {
        double dL = Math.abs(left) - Math.abs(rob_L);
        double dR = Math.abs(right) - Math.abs(rob_R);
        if (Math.abs(left) <= RobotMap.DriveSmooth.logic_threshold_L && dL > 0 &&
                dL > RobotMap.DriveSmooth.tolerance_L) {
            rob_L += RobotMap.DriveSmooth.jump_L;
        } else {
            System.out.println("BYPASSED LEFT");
            // DebugUtil.assert_(dL <= 0, "dL > 0 and still bypassed");
            rob_L = left;
        }
        if (Math.abs(right) <= RobotMap.DriveSmooth.logic_threshold_R && dR > 0 &&
                dR > RobotMap.DriveSmooth.tolerance_R) {
            rob_R += RobotMap.DriveSmooth.jump_R;
        } else {
            System.out.println("BYPASSED RIGHT");
            // DebugUtil.assert_(dR <= 0, "dR > 0 and still bypassed");
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
        // Disallow setting motors while disabled
        if (!get_enabled()) {
            return;
        }
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
        // Disallow setting motors while disabled
        if (!get_enabled()) {
            return;
        }
        if (smooth) {
            smooth_current(left, right);
        } else {
            // System.out.println("NOT USING SMOOTH");
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

    public void poll_controls() {
        HIDAxisAccessor y_axis = Control.get_instance().action2axis(Action.A_DRIVE_Y);
        HIDAxisAccessor z_axis = Control.get_instance().action2axis(Action.A_DRIVE_Z);
        HIDAxisAccessor l_axis = Control.get_instance().action2axis(Action.A_DRIVE_L);
        HIDAxisAccessor r_axis = Control.get_instance().action2axis(Action.A_DRIVE_R);
        if (l_axis != null && r_axis != null) {
            Drive.get_instance().set_tank(-l_axis.get(), -r_axis.get(), false, true);
        } else if (y_axis != null && z_axis != null) {
            Drive.get_instance().set_arcade(-y_axis.get(), z_axis.get(), false, true);
        } else {
            LOGGER.warning("Current KeyMap has no drive controls! Setting drive to (0, 0).");
            Drive.get_instance().set_tank(0, 0, false, true);
        }
    }

    // TODO: Accessors for CAN data on motors, ex: current output

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_TURN_ANGLE_TOLERANCE_MS",
            "O_STRAIGHT_DISTANCE_TOLERANCE_MS",
            "O_CAN_ID_FRONT_LEFT_MOTOR_NS",
            "O_CAN_ID_FRONT_RIGHT_MOTOR_NS",
            "O_CAN_ID_BACK_LEFT_MOTOR_NS",
            "O_CAN_ID_BACK_RIGHT_MOTOR_NS",
            "O_DIO_ENCODER_LEFT_A_NS",
            "O_DIO_ENCODER_LEFT_B_NS",
            "O_DIO_ENCODER_RIGHT_A_NS",
            "O_DIO_ENCODER_RIGHT_B_NS",
            "O_DRIVE_SMOOTH_LOGIC_THRESHOLD_LEFT_MS",
            "O_DRIVE_SMOOTH_LOGIC_THRESHOLD_RIGHT_MS",
            "O_DRIVE_SMOOTH_TOLERANCE_LEFT_MS",
            "O_DRIVE_SMOOTH_TOLERANCE_RIGHT_MS",
            "O_DRIVE_SMOOTH_JUMP_LEFT_MS",
            "O_DRIVE_SMOOTH_JUMP_RIGHT_MS"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_TURN_ANGLE_TOLERANCE_MS":
                entry.setDouble(RobotMap.Maneuvers.turn_angle_tolerance);
                break;
            case "O_STRAIGHT_DISTANCE_TOLERANCE_MS":
                entry.setDouble(RobotMap.Maneuvers.straight_distance_tolerance);
                break;
            case "O_CAN_ID_FRONT_LEFT_MOTOR_NS":
                entry.setDouble(RobotMap.CAN.front_left_motor);
                break;
            case "O_CAN_ID_FRONT_RIGHT_MOTOR_NS":
                entry.setDouble(RobotMap.CAN.front_right_motor);
                break;
            case "O_CAN_ID_BACK_LEFT_MOTOR_NS":
                entry.setDouble(RobotMap.CAN.back_left_motor);
                break;
            case "O_CAN_ID_BACK_RIGHT_MOTOR_NS":
                entry.setDouble(RobotMap.CAN.back_right_motor);
                break;
            case "O_DIO_ENCODER_LEFT_A_NS":
                entry.setDouble(RobotMap.DIO.encoder_left_A);
                break;
            case "O_DIO_ENCODER_LEFT_B_NS":
                entry.setDouble(RobotMap.DIO.encoder_left_B);
                break;
            case "O_DIO_ENCODER_RIGHT_A_NS":
                entry.setDouble(RobotMap.DIO.encoder_right_A);
                break;
            case "O_DIO_ENCODER_RIGHT_B_NS":
                entry.setDouble(RobotMap.DIO.encoder_right_B);
                break;
            case "O_DRIVE_SMOOTH_LOGIC_THRESHOLD_LEFT_MS":
                entry.setDouble(RobotMap.DriveSmooth.logic_threshold_L);
                break;
            case "O_DRIVE_SMOOTH_LOGIC_THRESHOLD_RIGHT_MS":
                entry.setDouble(RobotMap.DriveSmooth.logic_threshold_R);
                break;
            case "O_DRIVE_SMOOTH_TOLERANCE_LEFT_MS":
                entry.setDouble(RobotMap.DriveSmooth.tolerance_L);
                break;
            case "O_DRIVE_SMOOTH_TOLERANCE_RIGHT_MS":
                entry.setDouble(RobotMap.DriveSmooth.tolerance_R);
                break;
            case "O_DRIVE_SMOOTH_JUMP_LEFT_MS":
                entry.setDouble(RobotMap.DriveSmooth.jump_L);
                break;
            case "O_DRIVE_SMOOTH_JUMP_RIGHT_MS":
                entry.setDouble(RobotMap.DriveSmooth.jump_R);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.SLOW, value = {
            "O_DRIVE_ENABLED_RT"
    })
    public void transmitter_slow(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_DRIVE_ENABLED_RT":
                entry.setBoolean(get_enabled());
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.FAST, value = {
            "O_DRIVE_LEFT_NT",
            "O_DRIVE_RIGHT_NT",
            "O_ACCEL_X_NT",
            "O_ACCEL_Y_NT",
            "O_ACCEL_Z_NT",
            "O_ENCODER_LEFT_NT",
            "O_ENCODER_RIGHT_NT"
    })
    public void transmitter_fast(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_DRIVE_LEFT_NT":
                entry.setDouble(rob_L);
                break;
            case "O_DRIVE_RIGHT_NT":
                entry.setDouble(rob_R);
                break;
            case "O_ACCEL_X_NT":
                entry.setDouble(get_accel_X());
                break;
            case "O_ACCEL_Y_NT":
                entry.setDouble(get_accel_Y());
                break;
            case "O_ACCEL_Z_NT":
                entry.setDouble(get_accel_Z());
                break;
            case "O_ENCODER_LEFT_NT":
                entry.setDouble(get_Lenc());
                break;
            case "O_ENCODER_RIGHT_NT":
                entry.setDouble(get_Renc());
                break;
        }
    }

    @SuppressWarnings("unused")
    @Receiver({
            "I_TURN_ANGLE_TOLERANCE",
            "I_STRAIGHT_DISTANCE_TOLERANCE",
            "I_DRIVE_SMOOTH_LOGIC_THRESHOLD_LEFT",
            "I_DRIVE_SMOOTH_LOGIC_THRESHOLD_RIGHT",
            "I_DRIVE_SMOOTH_TOLERANCE_LEFT",
            "I_DRIVE_SMOOTH_TOLERANCE_RIGHT",
            "I_DRIVE_SMOOTH_JUMP_LEFT",
            "I_DRIVE_SMOOTH_JUMP_RIGHT"
    })
    public void receiver(EntryNotification event) {
        switch (event.name) {
            case "I_TURN_ANGLE_TOLERANCE":
                if (event.value.isDouble()) {
                    RobotMap.Maneuvers.turn_angle_tolerance = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 360.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_STRAIGHT_DISTANCE_TOLERANCE":
                if (event.value.isDouble()) {
                    // Limit distance tolerance to [0, 10] metres (10 metres is already a ridiculous upper bound)
                    RobotMap.Maneuvers.straight_distance_tolerance = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 10.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_DRIVE_SMOOTH_LOGIC_THRESHOLD_LEFT":
                if (event.value.isDouble()) {
                    RobotMap.DriveSmooth.logic_threshold_L = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_DRIVE_SMOOTH_LOGIC_THRESHOLD_RIGHT":
                if (event.value.isDouble()) {
                    RobotMap.DriveSmooth.logic_threshold_R = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_DRIVE_SMOOTH_TOLERANCE_LEFT":
                if (event.value.isDouble()) {
                    RobotMap.DriveSmooth.tolerance_L = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_DRIVE_SMOOTH_TOLERANCE_RIGHT":
                if (event.value.isDouble()) {
                    RobotMap.DriveSmooth.tolerance_R = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_DRIVE_SMOOTH_JUMP_LEFT":
                if (event.value.isDouble()) {
                    RobotMap.DriveSmooth.jump_L = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_DRIVE_SMOOTH_JUMP_RIGHT":
                if (event.value.isDouble()) {
                    RobotMap.DriveSmooth.jump_R = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
        }
    }

    @SuppressWarnings("unused")
    @RPCCaller("R_DRIVE_ENABLE")
    public void rpccaller_enable(RpcAnswer rpc) {
        if (rpc.params.getBytes()[0] == 1) {
            LOGGER.info("Drive enabled via RPC.");
            set_enabled(true);
            rpc.postResponse(new byte[] {1});  // Success
        } else if (rpc.params.getBytes()[0] == 0) {
            LOGGER.info("Drive disabled via RPC.");
            set_enabled(false);
            rpc.postResponse(new byte[] {1});  // Success
        }
        rpc.postResponse(new byte[] {0});  // Failure
    }
}
