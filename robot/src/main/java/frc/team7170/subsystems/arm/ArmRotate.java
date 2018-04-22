package frc.team7170.subsystems.arm;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Spark;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.JRunnable;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.Pneumatics;

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
        Pneumatics.get_instance().set_solenoids(false);
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_home));
    }

    public void go_to_base_position() {
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_base));
        // Extend the arm after getting to the base position
        Dispatcher.get_instance().add_job(new JRunnable(() -> Pneumatics.get_instance().set_solenoids(true), this));
    }

    public void go_to_switch_position() {
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_switch));
    }

    public void go_to_scale_position() {
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_scale));
    }

    public void go_to_reverse_position() {
        Pneumatics.get_instance().set_solenoids(false);
        Dispatcher.get_instance().add_job(new JMoveArm(RobotMap.Arm.pot_value_reverse));
    }

}
