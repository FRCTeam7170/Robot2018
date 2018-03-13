package frc.team7170.subsystems.arm;

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
    }

    @Override
    public String toString() {
        return "JMoveArm("+degree+")";
    }
}
