package frc.team7170.subsystems.arm;

import java.util.logging.Logger;

import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import frc.team7170.comm.Receiver;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Pneumatics;
import frc.team7170.util.CalcUtil;


/**
 * Module to handle all interactions with the arm of the robot. This includes the end effector wheel motors, the motors
 * that power the arm rotation, and the pneumatic arms.
 */
public class Arm extends Module {

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
    }

    private Spark spark_left_endE = new Spark(RobotMap.PWM.endE_left_motor);
    private Spark spark_right_endE = new Spark(RobotMap.PWM.endE_right_motor);
    private Spark spark_left_arm = new Spark(RobotMap.PWM.arm_left_motor);
    private Spark spark_right_arm = new Spark(RobotMap.PWM.arm_right_motor);

    private AnalogPotentiometer pot = new AnalogPotentiometer(RobotMap.AIO.arm_pot, RobotMap.Arm.pot_scale, RobotMap.Arm.pot_offset);

    private boolean extended = false;

    @Override
    protected void update() {
        if (extended && in_inner_thresh()) {
            extended = false;
            Pneumatics.get_instance().set_solenoids(false);
        } else if (!extended && !in_outer_thresh()) {
            extended = true;
            Pneumatics.get_instance().set_solenoids(true);
        }
    }

    @Override
    protected void enabled() {
        LOGGER.info("Arm enabled.");
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
        return "Arm module.";
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

    public double get_pot_val() {
        return pot.get();
    }

    public void endE_suck() {
        spark_left_endE.set(RobotMap.Arm.endE_speed);
        spark_right_endE.set(RobotMap.Arm.endE_speed);
    }

    public void endE_push() {
        spark_left_endE.set(-RobotMap.Arm.endE_speed);
        spark_right_endE.set(-RobotMap.Arm.endE_speed);
    }

    public void endE_kill() {
        spark_left_endE.set(0);
        spark_right_endE.set(0);
    }

    public void endE_analog(double speed) {
        spark_left_endE.set(speed);
        spark_right_endE.set(speed);
    }

    public double get_endE_speed() {
        return spark_left_endE.get();  // Doesn't matter which
    }

    public void arm_up() {
        spark_left_arm.set(RobotMap.Arm.arm_speed);
        spark_right_arm.set(RobotMap.Arm.arm_speed);
    }

    public void arm_down() {
        spark_left_arm.set(-RobotMap.Arm.arm_speed);
        spark_right_arm.set(-RobotMap.Arm.arm_speed);
    }

    public void arm_kill() {
        spark_left_arm.set(0);
        spark_right_arm.set(0);
    }

    public void arm_analog(double speed) {
        spark_left_arm.set(speed);
        spark_right_arm.set(speed);
    }

    public double get_arm_speed() {
        return spark_left_arm.get();  // Doesn't matter which
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.STATIC, value = {
            "O_PWM_ARM_LEFT_MOTOR_S",
            "O_PWM_ARM_RIGHT_MOTOR_S",
            "O_PWM_ENDE_LEFT_MOTOR_S",
            "O_PWM_ENDE_RIGHT_MOTOR_S",
            "O_AIO_ARM_POT_S",
            "O_ARM_POT_SCALE_M",
            "O_ARM_POT_OFFSET_M",
            "O_ARM_SPEED_M",
            "O_ENDE_SPEED_M",
            "O_ENDE_REVERSE_LEFT_M",
            "O_ENDE_REVERSE_RIGHT_M",
            "O_ARM_POT_KILL_VAL_LOWER_INNER_M",
            "O_ARM_POT_KILL_VAL_UPPER_INNER_M",
            "O_ARM_POT_KILL_VAL_LOWER_OUTER_M",
            "O_ARM_POT_KILL_VAL_UPPER_OUTER_M"
    })
    public void transmitter_static(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_PWM_ARM_LEFT_MOTOR_S":
                entry.setDouble(RobotMap.PWM.arm_left_motor);
                break;
            case "O_PWM_ARM_RIGHT_MOTOR_S":
                entry.setDouble(RobotMap.PWM.arm_right_motor);
                break;
            case "O_PWM_ENDE_LEFT_MOTOR_S":
                entry.setDouble(RobotMap.PWM.endE_left_motor);
                break;
            case "O_PWM_ENDE_RIGHT_MOTOR_S":
                entry.setDouble(RobotMap.PWM.endE_right_motor);
                break;
            case "O_AIO_ARM_POT_S":
                entry.setDouble(RobotMap.AIO.arm_pot);
                break;
            case "O_ARM_POT_SCALE_M":
                entry.setDouble(RobotMap.Arm.pot_scale);
                break;
            case "O_ARM_POT_OFFSET_M":
                entry.setDouble(RobotMap.Arm.pot_offset);
                break;
            case "O_ARM_SPEED_M":
                entry.setDouble(RobotMap.Arm.arm_speed);
                break;
            case "O_ENDE_SPEED_M":
                entry.setDouble(RobotMap.Arm.endE_speed);
                break;
            case "O_ENDE_REVERSE_LEFT_M":
                entry.setBoolean(RobotMap.Arm.reverse_endE_left);
                break;
            case "O_ENDE_REVERSE_RIGHT_M":
                entry.setBoolean(RobotMap.Arm.reverse_endE_right);
                break;
            case "O_ARM_POT_KILL_VAL_LOWER_INNER_M":
                entry.setDouble(RobotMap.Arm.pot_value_kill_lower_inner);
                break;
            case "O_ARM_POT_KILL_VAL_UPPER_INNER_M":
                entry.setDouble(RobotMap.Arm.pot_value_kill_upper_inner);
                break;
            case "O_ARM_POT_KILL_VAL_LOWER_OUTER_M":
                entry.setDouble(RobotMap.Arm.pot_value_kill_lower_outer);
                break;
            case "O_ARM_POT_KILL_VAL_UPPER_OUTER_M":
                entry.setDouble(RobotMap.Arm.pot_value_kill_upper_outer);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.FAST, value = {
            "O_ARM_POT_VAL_S",
            "O_ARM_CURR_SPEED_S",
            "O_ENDE_CURR_SPEED_S"
    })
    public void transmitter_fast(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_ARM_POT_VAL_S":
                entry.setDouble(get_pot_val());
                break;
            case "O_ARM_CURR_SPEED_S":
                entry.setDouble(get_arm_speed());
                break;
            case "O_ENDE_CURR_SPEED_S":
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
}
