package frc.team7170.subsystems.arm;

import edu.wpi.first.wpilibj.Spark;
import frc.team7170.control.Action;
import frc.team7170.control.Control;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.control.HIDButtonAccessor;
import frc.team7170.jobs.Dispatcher;
import frc.team7170.jobs.Module;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;

import java.util.logging.Logger;

public class ArmEndE extends Module {

    private final static Logger LOGGER = Logger.getLogger(ArmEndE.class.getName());

    private static ArmEndE instance = new ArmEndE();  // Singleton
    public static ArmEndE get_instance() {
        return instance;
    }
    private ArmEndE() {
        LOGGER.info("Initializing arm end effector system.");

        spark_left_endE.setInverted(RobotMap.Arm.reverse_endE_left);
        spark_right_endE.setInverted(RobotMap.Arm.reverse_endE_right);

        Dispatcher.get_instance().register_module(this);
    }

    private Spark spark_left_endE = new Spark(RobotMap.PWM.endE_left_motor);
    private Spark spark_right_endE = new Spark(RobotMap.PWM.endE_right_motor);

    @Override
    protected void enabled() {
        LOGGER.info("Arm end effector enabled.");
    }

    @Override
    protected void disabled() {
        LOGGER.info("Arm end effector disabled.");
        endE_kill();
    }

    @Override
    public String toString() {
        return "Arm End Effector System";
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

    /**
     * Poll the controls involved with this system for when the robot is in teleop mode.
     * Note the "tiered" layout (i.e. the order the buttons/axes are checked): all the various input sources for a
     * certain system have a precedence.
     */
    public void poll_controls() {
        HIDAxisAccessor endE_axis = Control.get_instance().action2axis(Action.A_ENDE_ANALOG);
        if (endE_axis != null) {
            endE_analog(endE_axis.get());
        } else {
            HIDAxisAccessor endE_axis_push = Control.get_instance().action2axis(Action.A_ENDE_ANALOG_PUSH);
            HIDAxisAccessor endE_axis_suck = Control.get_instance().action2axis(Action.A_ENDE_ANALOG_SUCK);
            if (endE_axis_push != null && endE_axis_suck != null) {
                if (!CalcUtil.in_threshold(endE_axis_push.get(), 0, RobotMap.Arm.endE_analog_ignore_thresh)) {
                    endE_analog(-endE_axis_push.get());
                } else if (!CalcUtil.in_threshold(endE_axis_suck.get(), 0, RobotMap.Arm.endE_analog_ignore_thresh)) {
                    endE_analog(endE_axis_suck.get());
                } else {
                    endE_kill();
                }
            } else {
                HIDButtonAccessor endE_push_btn = Control.get_instance().action2button(Action.B_ENDE_PUSH);
                HIDButtonAccessor endE_suck_btn = Control.get_instance().action2button(Action.B_ENDE_SUCK);
                if (endE_push_btn != null && endE_push_btn.get()) {
                    endE_push();
                } else if (endE_suck_btn != null && endE_suck_btn.get()) {
                    endE_suck();
                } else {
                    endE_kill();
                }
            }
        }
    }
}
