package frc.team7170.subsystems.arm;

import frc.team7170.jobs.Job;


public class JHoldArm extends Job {

    private double degree;
    private int stall_ms;
    private long start_time;

    public JHoldArm() {
        this(-1);
    }

    public JHoldArm(int stall_ms) {
        this.degree = Arm.get_instance().get_pot_val();
        requires(Arm.get_instance());
        this.stall_ms = stall_ms;
    }

    @Override
    protected void init() {
        start_time = System.currentTimeMillis();
    }

    @Override
    protected void update() {
        if (Arm.get_instance().get_pot_val() > degree) {
            Arm.get_instance().arm_down();
        } else if (Arm.get_instance().get_pot_val() < degree) {
            Arm.get_instance().arm_up();
        } else {
            Arm.get_instance().arm_kill();
        }
    }

    @Override
    protected boolean is_finished() {
        if (stall_ms == -1) {
            return false;
        }
        return System.currentTimeMillis() >= start_time + stall_ms;
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
        return "JHoldArm()";
    }
}
