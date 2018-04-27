package frc.team7170.subsystems.arm;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Spark;
import frc.team7170.control.Action;
import frc.team7170.control.Control;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.control.HIDButtonAccessor;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.JRunnable;
import frc.team7170.jobs.Job;
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
        spark_left_arm.set(RobotMap.Arm.arm_speed_multiplier*speed);
        spark_right_arm.set(RobotMap.Arm.arm_speed_multiplier*speed);
    }

    public double get_arm_speed() {
        return spark_left_arm.get();  // Doesn't matter which
    }

    public void go_to_home_position() {
        stop_hold_arm();
        Pneumatics.get_instance().set_solenoids(false);
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_home));
        hold_arm();
    }

    public void go_to_base_position() {
        stop_hold_arm();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_base));
        // Extend the arm after getting to the base position
        Dispatcher.get_instance().add_job(new JRunnable(() -> Pneumatics.get_instance().set_solenoids(true), this));
        hold_arm();
    }

    public void go_to_switch_position() {
        stop_hold_arm();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_switch));
        hold_arm();
    }

    public void go_to_scale_position() {
        stop_hold_arm();
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_scale));
        hold_arm();
    }

    public void go_to_reverse_position() {
        stop_hold_arm();
        Pneumatics.get_instance().set_solenoids(false);
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_reverse));
        hold_arm();
    }

    private Job teleop_hold_arm;

    private void hold_arm() {
        if (teleop_hold_arm == null) {
            teleop_hold_arm = new JHoldArm();
            Dispatcher.get_instance().add_job(teleop_hold_arm);
        }
    }

    private void stop_hold_arm() {
        if (teleop_hold_arm != null) {
            Dispatcher.get_instance().cancel_job(teleop_hold_arm, true);
            teleop_hold_arm = null;
        }
    }

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
}
