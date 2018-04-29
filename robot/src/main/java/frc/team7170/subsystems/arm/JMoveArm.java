package frc.team7170.subsystems.arm;

import frc.team7170.jobs.Job;
import frc.team7170.robot.RobotMap;
import frc.team7170.subsystems.drive.Acceleration;
import frc.team7170.util.CalcUtil;


public class JMoveArm extends Job {

    private double degree;
    private final Acceleration accel;
    private double start_degree;
    private boolean moving_down;

    public JMoveArm(double degree, double max_out, double transition_in, double transition_out, double stop_accel,
                    double start_decel, boolean lin_accel, boolean lin_decel) {
        this.degree = CalcUtil.apply_bounds(degree, 0, 300);
        this.moving_down = degree < ArmRotate.get_instance().get_pot_val();
        this.accel = new Acceleration(max_out, transition_in, transition_out, stop_accel, start_decel, lin_accel,
                lin_decel, moving_down);
        requires(ArmRotate.get_instance());
    }

    @Override
    protected void init() {
        start_degree = ArmRotate.get_instance().get_pot_val();
    }

    @Override
    protected void update() {
        double curr_angle = ArmRotate.get_instance().get_pot_val();
        ArmRotate.get_instance().arm_analog(accel.get(Math.abs(start_degree - curr_angle)/Math.abs(start_degree - degree)));
    }

    @Override
    protected boolean is_finished() {
        return CalcUtil.in_threshold(ArmRotate.get_instance().get_pot_val(), degree, RobotMap.Arm.move_arm_pot_tolerance) ||
                (moving_down ^ (ArmRotate.get_instance().get_pot_val() >= degree));
    }

    @Override
    protected void interrupted() {
        end();
    }

    @Override
    protected void end() {
        // Note that transition_out isn't honoured as the actual final speed; it's only used for calculating deceleration rate.
        ArmRotate.get_instance().arm_kill();
    }

    @Override
    public String toString() {
        return "JMoveArm("+degree+")";
    }
}
