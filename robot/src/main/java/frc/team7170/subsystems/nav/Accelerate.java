package frc.team7170.subsystems.nav;


public class Accelerate {

    private boolean lin_accel;
    private boolean lin_decel;
    private boolean max_reached = false;
    private double max_out;
    private double transition_out;
    private double transition_in;
    private double stop_accel;
    private double start_decel;
    private double prev_out;
    private double prev_in = 0;
    private double max_reached_at_lin;

    public Accelerate(double max_out, double transition_in, double transition_out, double stop_accel,
                      double start_decel, boolean lin_accel, boolean lin_decel) {
        this.max_out = max_out;
        this.transition_in = transition_in;
        this.transition_out = transition_out;
        this.stop_accel = stop_accel;
        this.start_decel = start_decel;
        this.lin_accel = lin_accel;
        this.lin_decel = lin_decel;
        this.prev_out = transition_in;
    }

    double get(double prog) {
        if (!max_reached & prog < start_decel) {  // Accelerate
            prev_out = lin_accel ? accel_lin(prog) : accel_const(prog);
            if (prev_out >= max_out) {
                max_reached = true;  // Stop accelerating
                prev_out = max_out;
            }
        }
        if (prog >= start_decel) {  // Decelerate
            prev_out = lin_decel ? decel_lin(prog) : decel_const(prog);
        }
        prev_in = prog;
        return prev_out;
    }

    private double accel_const(double prog) {
        return (max_out - prev_out)/(stop_accel - prev_in) * prog + transition_in;
    }

    private double decel_const(double prog) {
        return (transition_out - prev_out)/(1 - prev_in) * (prog - 1) + transition_out;
    }

    private double accel_lin(double prog) {
        return (max_out - transition_in)/(stop_accel*stop_accel) * prog*prog + transition_in;
    }

    private double decel_lin(double prog) {
        if (max_reached_at_lin != 0) {
            max_reached_at_lin = prev_out;
        }
        return (max_reached_at_lin - transition_out)/((start_decel - 1)*(start_decel - 1)) * (prog - 1)*(prog - 1) + transition_out;
    }
}
