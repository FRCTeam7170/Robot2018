package frc.team7170.subsystems.arm;

import edu.wpi.first.wpilibj.DriverStation;
import frc.team7170.control.Action;
import frc.team7170.control.Control;
import frc.team7170.control.HIDAxisAccessor;
import frc.team7170.control.HIDButtonAccessor;
import frc.team7170.jobs.Job;
import frc.team7170.robot.RobotMap;
import frc.team7170.util.CalcUtil;


public class JMoveArm extends Job {

    private double degree;
    private boolean moving_up;

    public JMoveArm(double degree) {
        this.degree = CalcUtil.apply_bounds(degree, RobotMap.Arm.pot_offset, RobotMap.Arm.pot_scale+RobotMap.Arm.pot_offset);
        requires(Arm.get_instance());
    }

    @Override
    protected void init() {
        moving_up = Arm.get_instance().get_pot_val() < degree;
        // TODO: This is gross, need better solution to prevent driver from interfering during job
        if (DriverStation.getInstance().isOperatorControl()) {
            HIDButtonAccessor extend_btn = Control.get_instance().action2button(Action.B_TRY_ARM_EXTEND);
            HIDButtonAccessor retract_btn = Control.get_instance().action2button(Action.B_ARM_RETRACT);
            HIDButtonAccessor toggle_btn = Control.get_instance().action2button(Action.B_TRY_ARM_TOGGLE);
            HIDButtonAccessor arm_up = Control.get_instance().action2button(Action.B_ARM_UP);
            HIDButtonAccessor arm_down = Control.get_instance().action2button(Action.B_ARM_DOWN);
            HIDAxisAccessor arm_axis = Control.get_instance().action2axis(Action.A_ARM_ANALOG);
            if (extend_btn != null) {
                extend_btn.simulate(false);
            }
            if (retract_btn != null) {
                retract_btn.simulate(false);
            }
            if (toggle_btn != null) {
                toggle_btn.simulate(false);
            }
            if (arm_up != null) {
                arm_up.simulate(false);
            }
            if (arm_down != null) {
                arm_down.simulate(false);
            }
            if (arm_axis != null) {
                arm_axis.set_scale(0);
                arm_axis.set_offset(RobotMap.Arm.arm_speed);
            }
        }
    }

    @Override
    protected void update() {
        if (moving_up) {
            Arm.get_instance().arm_up();
        } else {
            Arm.get_instance().arm_down();
        }
    }

    @Override
    protected boolean is_finished() {
        return CalcUtil.in_threshold(Arm.get_instance().get_pot_val(), degree, RobotMap.Arm.move_arm_pot_tolerance);
    }

    @Override
    protected void interrupted() {
        end();
    }

    @Override
    protected void end() {
        Arm.get_instance().arm_kill();
        if (DriverStation.getInstance().isOperatorControl()) {
            HIDButtonAccessor extend_btn = Control.get_instance().action2button(Action.B_TRY_ARM_EXTEND);
            HIDButtonAccessor retract_btn = Control.get_instance().action2button(Action.B_ARM_RETRACT);
            HIDButtonAccessor toggle_btn = Control.get_instance().action2button(Action.B_TRY_ARM_TOGGLE);
            HIDButtonAccessor arm_up = Control.get_instance().action2button(Action.B_ARM_UP);
            HIDButtonAccessor arm_down = Control.get_instance().action2button(Action.B_ARM_DOWN);
            HIDAxisAccessor arm_axis = Control.get_instance().action2axis(Action.A_ARM_ANALOG);
            if (extend_btn != null) {
                extend_btn.stop_simulate();
            }
            if (retract_btn != null) {
                retract_btn.stop_simulate();
            }
            if (toggle_btn != null) {
                toggle_btn.stop_simulate();
            }
            if (arm_up != null) {
                arm_up.stop_simulate();
            }
            if (arm_down != null) {
                arm_down.stop_simulate();
            }
            if (arm_axis != null) {
                arm_axis.reset();
            }
        }
    }

    @Override
    public String toString() {
        return "JMoveArm("+degree+")";
    }
}
