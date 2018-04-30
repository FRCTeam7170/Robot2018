package frc.team7170.subsystems.arm;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Spark;
import frc.team7170.control.Action;
import frc.team7170.control.Control;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.control.HIDButtonAccessor;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.JRunnable;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Pneumatics;
import frc.team7170.util.CalcUtil;

import java.util.logging.Logger;

public class ArmRotate extends Module {

    private final static Logger LOGGER = Logger.getLogger(ArmRotate.class.getName());

    private static ArmRotate instance = new ArmRotate();  // Singleton
    public static ArmRotate get_instance() {
        return instance;
    }
    private ArmRotate() {
        LOGGER.info("Initializing arm rotate system.");

        spark_left_arm.setInverted(RobotMap.Arm.reverse_arm_left);
        spark_right_arm.setInverted(RobotMap.Arm.reverse_arm_right);

        Dispatcher.get_instance().register_module(this);
    }

    private Spark spark_left_arm = new Spark(RobotMap.PWM.arm_left_motor);
    private Spark spark_right_arm = new Spark(RobotMap.PWM.arm_right_motor);

    private AnalogPotentiometer pot = new AnalogPotentiometer(RobotMap.AIO.arm_pot, RobotMap.Arm.pot_scale, RobotMap.Arm.pot_offset);

    @Override
    protected void update() {
        // Make sure the arm never breaks the 16 in. perimeter plane.
        if (Pneumatics.get_instance().get_solenoids() && in_inner_thresh()) {
            Pneumatics.get_instance().set_solenoids(false);
        }
    }

    @Override
    protected void enabled() {
        LOGGER.info("Arm rotate enabled.");
        Pneumatics.get_instance().set_enabled(true);
    }

    @Override
    protected void disabled() {
        LOGGER.info("Arm rotate disabled.");
        Pneumatics.get_instance().set_enabled(false);
        arm_kill();
    }

    @Override
    public String toString() {
        return "Arm Rotate System";
    }

    /**
     * @return If the arm is in the inner thresholds.
     */
    public boolean in_inner_thresh() {
        double pot_read = pot.get();
        return (pot_read >= RobotMap.Arm.pot_value_kill_lower_inner &&
                pot_read <= RobotMap.Arm.pot_value_kill_upper_inner);
    }

    /**
     * @return If the arm is in the outer thresholds.
     */
    public boolean in_outer_thresh() {
        double pot_read = pot.get();
        return (pot_read >= RobotMap.Arm.pot_value_kill_lower_outer &&
                pot_read <= RobotMap.Arm.pot_value_kill_upper_outer);
    }

    /**
     * @return If extending the arm would result in physically hitting the robot base (not good, so don't do it!).
     */
    public boolean base_conflicting_extend() {
        return pot.get() < RobotMap.Arm.pot_value_base_conflict;
    }

    /**
     * Attempt to extend the arm. Fails if the arm will collide with the base or go out of perimeter.
     * @return If the extend was successful.
     */
    public boolean try_extend() {
        if (Pneumatics.get_instance().get_solenoids() || base_conflicting_extend() || in_inner_thresh()) {
            return false;
        }
        Pneumatics.get_instance().set_solenoids(true);
        return true;
    }

    /**
     * Retract the arm.
     * @return If the retract was successful.
     */
    public boolean retract() {
        return Pneumatics.get_instance().set_solenoids(false);
    }

    /**
     * Attempt to toggle the arm. Will always succeed if the arm is currently extended (unless the robot is disabled);
     * fails if the arm is retracted and the arm will collide with the base or break the perimeter plane.
     * @return If the toggle was successful.
     */
    public boolean try_toggle() {
        if (Pneumatics.get_instance().get_solenoids()) {
            return retract();
        }
        return try_extend();
    }

    /**
     * @return The current arm potentiometer reading as degrees off of the starting (limp) position.
     */
    public double get_pot_val() {
        return pot.get();
    }

    /**
     * Move the arm up at a set speed. Note that the multiplier in RobotMap is applied here.
     */
    public void arm_up() {
        arm_analog(RobotMap.Arm.arm_speed);
    }

    /**
     * Move the arm down at a set speed. Note that the multiplier in RobotMap is applied here.
     */
    public void arm_down() {
        arm_analog(-RobotMap.Arm.arm_speed);
    }

    /**
     * Turn the arm rotation off.
     */
    public void arm_kill() {
        arm_analog(0);
    }

    /**
     * Set the arm rotate motors to a given speed. Note a multiplier is applied lest the arm moves too jerkily.
     * @param speed The speed to set in [0,1].
     */
    public void arm_analog(double speed) {
        if (!get_enabled()) {
            return;
        }
        spark_left_arm.set(RobotMap.Arm.arm_speed_multiplier*speed);
        spark_right_arm.set(RobotMap.Arm.arm_speed_multiplier*speed);
    }

    /**
     * @return The arm's current speed.
     */
    public double get_arm_speed() {
        return spark_left_arm.get();  // Doesn't matter which side
    }

    /**
     * Move the arm to the "home" position (position the robot starts in).
     */
    public void go_to_home_position() {
        stop_hold_arm();
        Pneumatics.get_instance().set_solenoids(false);
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_home, 0.6, 0.5, 0.5, 0.3, 0.7, false, false));
        hold_arm();
    }

    /**
     * Move the arm to the "base" position (position used when sucking up cubes).
     */
    public void go_to_base_position() {
        stop_hold_arm();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_base, 0.6, 0.5, 0.5, 0.3, 0.7, false, false));
        // Extend the arm after getting to the base position
        Dispatcher.get_instance().add_job(new JRunnable(() -> Pneumatics.get_instance().set_solenoids(true), this));
        hold_arm();
    }

    /**
     * Move the arm to the switch position (position used when shooting cubes into the switch).
     */
    public void go_to_switch_position() {
        stop_hold_arm();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_switch, 0.6, 0.5, 0.5, 0.3, 0.7, false, false));
        hold_arm();
    }

    /**
     * Move the arm to the scale position (position used when shooting cubes into the scale).
     */
    public void go_to_scale_position() {
        stop_hold_arm();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_scale, 0.6, 0.5, 0.5, 0.3, 0.7, false, false));
        hold_arm();
    }

    /**
     * Move the arm to the "reverse" position (position used to shoot cubes into the switch backwards).
     */
    public void go_to_reverse_position() {
        stop_hold_arm();
        Pneumatics.get_instance().set_solenoids(false);
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_reverse, 0.6, 0.5, 0.5, 0.3, 0.7, false, false));
        hold_arm();
    }

    /**
     * Holds the {@link JHoldArm} that is ran during teleop essentially whenever no arm input is provided.
     */
    private JHoldArm teleop_hold_arm;

    /**
     * Instantiate a new {@link JHoldArm} if the current one no longer exists (i.e. if the arm was moved since the
     * instantiation of the last job).
     */
    private void hold_arm() {
        if (teleop_hold_arm == null) {
            teleop_hold_arm = new JHoldArm();
            Dispatcher.get_instance().add_job(teleop_hold_arm);
        }
    }

    /**
     * Cancel the current {@link JHoldArm} to allow for manual arm motor input or otherwise.
     */
    private void stop_hold_arm() {
        if (teleop_hold_arm != null) {
            Dispatcher.get_instance().cancel_job(teleop_hold_arm, true);
            teleop_hold_arm = null;
        }
    }

    /**
     * Poll the controls involved with this system for when the robot is in teleop mode.
     * Note the "tiered" layout (i.e. the order the buttons/axes are checked): all the various input sources for a
     * certain system have a precedence.
     */
    public void poll_controls() {
        // Poll extension controls
        HIDButtonAccessor extend_btn = Control.get_instance().action2button(Action.B_TRY_ARM_EXTEND);
        HIDButtonAccessor retract_btn = Control.get_instance().action2button(Action.B_ARM_RETRACT);
        HIDButtonAccessor toggle_btn = Control.get_instance().action2button(Action.B_TRY_ARM_TOGGLE);
        if (extend_btn != null && extend_btn.get_pressed()) {
            try_extend();
        } else if (retract_btn != null && retract_btn.get_pressed()) {
            retract();
        } else if (toggle_btn != null && toggle_btn.get_pressed()) {
            try_toggle();
        }

        // Poll rotate controls
        HIDAxisAccessor arm_axis = Control.get_instance().action2axis(Action.A_ARM_ANALOG);
        if (arm_axis != null) {
            if (CalcUtil.in_threshold(arm_axis.get(), 0, RobotMap.Arm.arm_analog_ignore_thresh)) {
                hold_arm();
            } else {
                stop_hold_arm();
                arm_analog(arm_axis.get());
            }
        } else {
            HIDAxisAccessor arm_axis_up = Control.get_instance().action2axis(Action.A_ARM_ANALOG_UP);
            HIDAxisAccessor arm_axis_down = Control.get_instance().action2axis(Action.A_ARM_ANALOG_DOWN);
            if (arm_axis_up != null && arm_axis_down != null) {
                if (!CalcUtil.in_threshold(arm_axis_up.get(), 0, RobotMap.Arm.arm_analog_ignore_thresh)) {
                    stop_hold_arm();
                    arm_analog(arm_axis_up.get());
                } else if (!CalcUtil.in_threshold(arm_axis_down.get(), 0, RobotMap.Arm.arm_analog_ignore_thresh)) {
                    stop_hold_arm();
                    arm_analog(-arm_axis_down.get());
                } else {
                    hold_arm();
                }
            } else {
                HIDButtonAccessor arm_up = Control.get_instance().action2button(Action.B_ARM_UP);
                HIDButtonAccessor arm_down = Control.get_instance().action2button(Action.B_ARM_DOWN);
                if (arm_up != null && arm_up.get()) {
                    stop_hold_arm();
                    arm_up();
                } else if (arm_down != null && arm_down.get()) {
                    stop_hold_arm();
                    arm_down();
                } else {
                    hold_arm();
                }

            }
        }

        // Poll preset positions
        HIDButtonAccessor base_pos_btn = Control.get_instance().action2button(Action.B_ARM_BASE);
        if (base_pos_btn != null && base_pos_btn.get_pressed()) {
            go_to_base_position();
        }
    }
    /*
    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_PWM_ARM_LEFT_MOTOR_NS",
            "O_PWM_ARM_RIGHT_MOTOR_NS",
            "O_PWM_ENDE_LEFT_MOTOR_NS",
            "O_PWM_ENDE_RIGHT_MOTOR_NS",
            "O_AIO_ARM_POT_NS",
            "O_ARM_POT_SCALE_MS",
            "O_ARM_POT_OFFSET_MS",
            "O_ARM_SPEED_MS",
            "O_ENDE_SPEED_MS",
            "O_ENDE_REVERSE_LEFT_MS",
            "O_ENDE_REVERSE_RIGHT_MS",
            "O_ARM_REVERSE_LEFT_MS",
            "O_ARM_REVERSE_RIGHT_MS",
            "O_ARM_POT_KILL_VAL_LOWER_INNER_MS",
            "O_ARM_POT_KILL_VAL_UPPER_INNER_MS",
            "O_ARM_POT_KILL_VAL_LOWER_OUTER_MS",
            "O_ARM_POT_KILL_VAL_UPPER_OUTER_MS"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_PWM_ARM_LEFT_MOTOR_NS":
                entry.setDouble(RobotMap.PWM.arm_left_motor);
                break;
            case "O_PWM_ARM_RIGHT_MOTOR_NS":
                entry.setDouble(RobotMap.PWM.arm_right_motor);
                break;
            case "O_PWM_ENDE_LEFT_MOTOR_NS":
                entry.setDouble(RobotMap.PWM.endE_left_motor);
                break;
            case "O_PWM_ENDE_RIGHT_MOTOR_NS":
                entry.setDouble(RobotMap.PWM.endE_right_motor);
                break;
            case "O_AIO_ARM_POT_NS":
                entry.setDouble(RobotMap.AIO.arm_pot);
                break;
            case "O_ARM_POT_SCALE_MS":
                entry.setDouble(RobotMap.Arm.pot_scale);
                break;
            case "O_ARM_POT_OFFSET_MS":
                entry.setDouble(RobotMap.Arm.pot_offset);
                break;
            case "O_ARM_SPEED_MS":
                entry.setDouble(RobotMap.Arm.arm_speed);
                break;
            case "O_ENDE_SPEED_MS":
                entry.setDouble(RobotMap.Arm.endE_speed);
                break;
            case "O_ENDE_REVERSE_LEFT_MS":
                entry.setBoolean(RobotMap.Arm.reverse_endE_left);
                break;
            case "O_ENDE_REVERSE_RIGHT_MS":
                entry.setBoolean(RobotMap.Arm.reverse_endE_right);
                break;
            case "O_ARM_REVERSE_LEFT_MS":
                entry.setBoolean(RobotMap.Arm.reverse_arm_left);
                break;
            case "O_ARM_REVERSE_RIGHT_MS":
                entry.setBoolean(RobotMap.Arm.reverse_arm_right);
                break;
            case "O_ARM_POT_KILL_VAL_LOWER_INNER_MS":
                entry.setDouble(RobotMap.Arm.pot_value_kill_lower_inner);
                break;
            case "O_ARM_POT_KILL_VAL_UPPER_INNER_MS":
                entry.setDouble(RobotMap.Arm.pot_value_kill_upper_inner);
                break;
            case "O_ARM_POT_KILL_VAL_LOWER_OUTER_MS":
                entry.setDouble(RobotMap.Arm.pot_value_kill_lower_outer);
                break;
            case "O_ARM_POT_KILL_VAL_UPPER_OUTER_MS":
                entry.setDouble(RobotMap.Arm.pot_value_kill_upper_outer);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.SLOW, value = {
            "O_ARM_ENABLED_RT"
    })
    public void transmitter_slow(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_ARM_ENABLED_RT":
                entry.setBoolean(get_enabled());
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.FAST, value = {
            "O_ARM_POT_VAL_NT",
            "O_ARM_CURR_SPEED_NT",
            "O_ENDE_CURR_SPEED_NT"
    })
    public void transmitter_fast(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_ARM_POT_VAL_NT":
                entry.setDouble(get_pot_val());
                break;
            case "O_ARM_CURR_SPEED_NT":
                entry.setDouble(get_arm_speed());
                break;
            case "O_ENDE_CURR_SPEED_NT":
                entry.setDouble(get_endE_speed());
                break;
        }
    }

    @SuppressWarnings("unused")
    @Receiver({
            "I_ARM_POT_SCALE",
            "I_ARM_POT_OFFSET",
            "I_ARM_SPEED",
            "I_ENDE_SPEED",
            "I_ENDE_REVERSE_LEFT",
            "I_ENDE_REVERSE_RIGHT",
            "I_ARM_REVERSE_LEFT",
            "I_ARM_REVERSE_RIGHT",
            "I_ARM_POT_KILL_VAL_LOWER_INNER",
            "I_ARM_POT_KILL_VAL_UPPER_INNER",
            "I_ARM_POT_KILL_VAL_LOWER_OUTER",
            "I_ARM_POT_KILL_VAL_UPPER_OUTER"
    })
    public void receiver(EntryNotification event) {
        switch (event.name) {
            case "I_ARM_POT_SCALE":
                if (event.value.isDouble()) {
                    RobotMap.Arm.pot_scale = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 360.0);
                    // Pot must be reinitialized as there does not exist any sort of .setScale() method.
                    pot.free();
                    pot = new AnalogPotentiometer(RobotMap.AIO.arm_pot, RobotMap.Arm.pot_scale, RobotMap.Arm.pot_offset);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_ARM_POT_OFFSET":
                if (event.value.isDouble()) {
                    RobotMap.Arm.pot_offset = CalcUtil.apply_bounds(event.value.getDouble(), -360.0, 360.0);
                    // Pot must be reinitialized as there does not exist any sort of .setOffset() method.
                    pot.free();
                    pot = new AnalogPotentiometer(RobotMap.AIO.arm_pot, RobotMap.Arm.pot_scale, RobotMap.Arm.pot_offset);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_ARM_SPEED":
                if (event.value.isDouble()) {
                    RobotMap.Arm.arm_speed = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_ENDE_SPEED":
                if (event.value.isDouble()) {
                    RobotMap.Arm.endE_speed = CalcUtil.apply_bounds(event.value.getDouble(), 0.0, 1.0);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_ENDE_REVERSE_LEFT":
                if (event.value.isBoolean()) {
                    RobotMap.Arm.reverse_endE_left = event.value.getBoolean();
                    spark_left_endE.setInverted(RobotMap.Arm.reverse_endE_left);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a boolean!");
                }
                break;
            case "I_ENDE_REVERSE_RIGHT":
                if (event.value.isBoolean()) {
                    RobotMap.Arm.reverse_endE_right = event.value.getBoolean();
                    spark_right_endE.setInverted(RobotMap.Arm.reverse_endE_right);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a boolean!");
                }
                break;
            case "I_ARM_REVERSE_LEFT":
                if (event.value.isBoolean()) {
                    RobotMap.Arm.reverse_arm_left = event.value.getBoolean();
                    spark_left_arm.setInverted(RobotMap.Arm.reverse_arm_left);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a boolean!");
                }
                break;
            case "I_ARM_REVERSE_RIGHT":
                if (event.value.isBoolean()) {
                    RobotMap.Arm.reverse_arm_right = event.value.getBoolean();
                    spark_right_arm.setInverted(RobotMap.Arm.reverse_arm_right);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a boolean!");
                }
                break;
            case "I_ARM_POT_KILL_VAL_LOWER_INNER":
                if (event.value.isDouble()) {
                    RobotMap.Arm.pot_value_kill_lower_inner = CalcUtil.apply_bounds(event.value.getDouble(),
                            RobotMap.Arm.pot_offset, RobotMap.Arm.pot_scale+RobotMap.Arm.pot_offset);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_ARM_POT_KILL_VAL_UPPER_INNER":
                if (event.value.isDouble()) {
                    RobotMap.Arm.pot_value_kill_upper_inner = CalcUtil.apply_bounds(event.value.getDouble(),
                            RobotMap.Arm.pot_offset, RobotMap.Arm.pot_scale+RobotMap.Arm.pot_offset);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_ARM_POT_KILL_VAL_LOWER_OUTER":
                if (event.value.isDouble()) {
                    RobotMap.Arm.pot_value_kill_lower_outer = CalcUtil.apply_bounds(event.value.getDouble(),
                            RobotMap.Arm.pot_offset, RobotMap.Arm.pot_scale+RobotMap.Arm.pot_offset);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
            case "I_ARM_POT_KILL_VAL_UPPER_OUTER":
                if (event.value.isDouble()) {
                    RobotMap.Arm.pot_value_kill_upper_outer = CalcUtil.apply_bounds(event.value.getDouble(),
                            RobotMap.Arm.pot_offset, RobotMap.Arm.pot_scale+RobotMap.Arm.pot_offset);
                } else {
                    LOGGER.severe(event.name+" entry updated but it is not a double!");
                }
                break;
        }
    }

    @SuppressWarnings("unused")
    @RPCCaller("R_ARM_ENABLE")
    public void rpccaller_enable(RpcAnswer rpc) {
        if (rpc.params.getBytes()[0] == 1) {
            LOGGER.info("Arm enabled via RPC.");
            set_enabled(true);
            rpc.postResponse(new byte[] {1});  // Success
        } else if (rpc.params.getBytes()[0] == 0) {
            LOGGER.info("Arm disabled via RPC.");
            set_enabled(false);
            rpc.postResponse(new byte[] {1});  // Success
        }
        rpc.postResponse(new byte[] {0});  // Failure
    }
    */
}
