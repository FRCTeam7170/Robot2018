package frc.team7170.subsystems.arm;

import java.util.logging.Logger;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.RpcAnswer;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import frc.team7170.comm.*;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.JRunnable;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Pneumatics;
import frc.team7170.util.CalcUtil;


/**
 * Module to handle all interactions with the arm of the robot. This includes the end effector wheel motors, the motors
 * that power the arm rotation, and the pneumatic arms.
 */
public class Arm extends Module implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(Arm.class.getName());

    private static Arm instance = new Arm();  // Singleton
    public static Arm get_instance() {
        return instance;
    }
    private Arm() {
        LOGGER.info("Initializing arm system.");

        spark_left_endE.setInverted(RobotMap.Arm.reverse_endE_left);
        spark_right_endE.setInverted(RobotMap.Arm.reverse_endE_right);

        Dispatcher.get_instance().register_module(this);
        register_comm();
    }

    private Spark spark_left_endE = new Spark(RobotMap.PWM.endE_left_motor);
    private Spark spark_right_endE = new Spark(RobotMap.PWM.endE_right_motor);
    private Spark spark_left_arm = new Spark(RobotMap.PWM.arm_left_motor);
    private Spark spark_right_arm = new Spark(RobotMap.PWM.arm_right_motor);

    private AnalogPotentiometer pot = new AnalogPotentiometer(RobotMap.AIO.arm_pot, RobotMap.Arm.pot_scale, RobotMap.Arm.pot_offset);

    @Override
    protected void update() {
        // TODO: Option to not always default to extend when out of danger zone (commented out for now)
        if (Pneumatics.get_instance().get_solenoids() && in_inner_thresh()) {
            Pneumatics.get_instance().set_solenoids(false);
        } /* else if (!Pneumatics.get_instance().get_solenoids() && !in_outer_thresh()) {
            Pneumatics.get_instance().set_solenoids(true);
        }*/
    }

    @Override
    protected void enabled() {
        LOGGER.info("Arm enabled.");
        Pneumatics.get_instance().set_enabled(true);
    }

    @Override
    protected void disabled() {
        LOGGER.info("Arm disabled.");
        // Also disabled pneumatics as it is essentially a part of the arm
        Pneumatics.get_instance().set_enabled(false);
        arm_kill();
        endE_kill();
    }

    @Override
    public String toString() {
        return "Arm System";
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

    public boolean try_extend() {
        if (Pneumatics.get_instance().get_solenoids() || base_conflicting_extend() || in_inner_thresh()) {
            return false;
        }
        Pneumatics.get_instance().set_solenoids(true);
        return true;
    }

    public boolean retract() {
        return Pneumatics.get_instance().set_solenoids(false);
    }

    public boolean try_toggle() {
        if (Pneumatics.get_instance().get_solenoids()) {
            return retract();
        }
        return try_extend();
    }

    public double get_pot_val() {
        return pot.get();
    }

    public void endE_suck() {
        endE_analog(RobotMap.Arm.endE_speed);
    }

    public void endE_push() {
        endE_analog(-RobotMap.Arm.endE_speed);
    }

    public void endE_kill() {
        endE_analog(0);
    }

    public void endE_analog(double speed) {
        if (!get_enabled()) {
            return;
        }
        spark_left_endE.set(speed);
        spark_right_endE.set(speed);
    }

    public double get_endE_speed() {
        return spark_left_endE.get();  // Doesn't matter which
    }

    public void arm_up() {
        arm_analog(RobotMap.Arm.arm_speed);
    }

    public void arm_down() {
        arm_analog(-RobotMap.Arm.arm_speed);
    }

    public void arm_kill() {
        arm_analog(0);
    }

    public void arm_analog(double speed) {
        if (!get_enabled()) {
            return;
        }
        spark_left_arm.set(speed);
        spark_right_arm.set(speed);
    }

    public double get_arm_speed() {
        return spark_left_arm.get();  // Doesn't matter which
    }

    public void go_to_home_position() {
        endE_kill();
        Pneumatics.get_instance().set_solenoids(false);
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_home));
    }

    public void go_to_base_position() {
        endE_kill();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_base));
        // Extend the arm after getting to the base position
        Dispatcher.get_instance().add_job(new JRunnable(() -> Pneumatics.get_instance().set_solenoids(true), this));
    }

    public void go_to_switch_position() {
        endE_kill();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_switch));
    }

    public void go_to_scale_position() {
        endE_kill();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_scale));
    }

    public void go_to_reverse_position() {
        endE_kill();
        Pneumatics.get_instance().set_solenoids(false);
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_reverse));
    }

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
}
