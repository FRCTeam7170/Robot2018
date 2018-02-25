package frc.team7170.jobs;

public abstract class Module {

    private boolean locked = false;
    private boolean enabled = true;

    private Job curr_job = null;
    private Job default_job = null;

    boolean claim_lock(Job job) {
        if (locked) {
            return false;
        }
        locked = true;
        curr_job = job;
        return true;
    }

    void free_lock() {
        locked = false;
        curr_job = null;
    }

    public final boolean get_lock() {
        return locked;
    }

    protected final Job get_current_job() {
        return curr_job;
    }

    protected final void set_default_job(Job job) {
        default_job = job;
    }

    protected final Job get_default_job() {
        return default_job;
    }

    public final void set_enabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            enabled();
        } else {
            disabled();
        }
    }

    public final boolean get_enabled() {
        return enabled;
    }

    protected void init() {}

    protected void update() {}

    protected void enabled() {}

    protected void disabled() {}

    // Enforce subclasses to implement meaningful string representations
    @Override
    public abstract String toString();
}
