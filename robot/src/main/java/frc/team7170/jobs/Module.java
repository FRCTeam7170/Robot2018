package frc.team7170.jobs;


/**
 * Defines a module/subsystem on the robot. Each module should have its own resources, such as motors or sensors.
 * Each module can only run one {@link Job} at a time.
 *
 * As an example, you may want to create subsystems for the robot base and some sort of claw or arm.
 *
 * Quick guidelines:
 * Call Dispatcher.register_module in the constructor of each module (this implies each module should be a singleton.)
 * Override the init, update, enabled, or disabled methods appropriately.
 * Use set_default_job to set the default job (runs when the dispatcher assigns no other job to this module.)
 * use set_enabled to enable/disable this module when it does/doesn't need to run.
 */
public abstract class Module {

    private boolean locked = false;  // State variable of whether the module's lock is claimed.
    private boolean enabled = true;  // State variable of whether the module is enabled.

    private Job curr_job = null;  // Holds the currently running job
    private Job default_job = null;  // Holds the default job to run if the dispatcher assigns no other job

    /**
     * Attempt to claim the lock on this module and assign it to the given {@link Job}.
     * @param job The job that claims this lock.
     * @return Whether the operation was successful (returns false if the lock was already engaged when this function is called)
     */
    boolean claim_lock(Job job) {
        if (locked) {
            return false;
        }
        locked = true;
        curr_job = job;
        if (curr_job != null) {
            // If the job this module was assigned to finishes while this module is disabled, the dispatcher
            // assigns a new job but the new job is unaware that its new parent is disabled. Hence we signal
            // the new job with the modules state whenever a job claims a module lock to avoid this problem.
            curr_job.signal_parent_state_change(this, !enabled);
        }
        return true;
    }

    /**
     * Frees this module's lock. Note that this doesn't actually stop a previously assigned {@link Job} from running.
     * The {@link Dispatcher} is responsible for signalling to a job that its parent has moved on from it (FeelsBadMan).
     */
    void free_lock() {
        locked = false;
        curr_job = null;
    }

    /**
     * @return Whether this module is currently locked or not.
     */
    public final boolean get_lock() {
        return locked;
    }

    /**
     * @return The current job running on this module (null if there is not current job or it's the default job).
     */
    protected final Job get_current_job() {
        return curr_job;
    }

    /**
     * Set the default {@link Job} to run while this module's lock is free.
     * @param job The job to set the default job to.
     */
    protected final void set_default_job(Job job) {
        default_job = job;
    }

    /**
     * @return The default job assigned to this module.
     */
    protected final Job get_default_job() {
        return default_job;
    }

    /**
     * Set the enabled state of this module. Disabled modules won't have their update() method executed
     * and {@link Job}s assigned to this module won't execute.
     * @param enabled Boolean state; on or off.
     */
    public final void set_enabled(boolean enabled) {
        if (enabled != this.enabled) {  // Only do anything if the argument is different from the current state.
            this.enabled = enabled;
            if (enabled) {
                enabled();
            } else {
                disabled();
            }
            if (curr_job != null) {
                // Signal the currently running job that the state of its parent changed.
                curr_job.signal_parent_state_change(this, !enabled);
            }
        }
    }

    /**
     * @return Whether this module is enabled or not.
     */
    public final boolean get_enabled() {
        return enabled;
    }

    /**
     * Called internally from {@link Dispatcher} to make sure that the update function is only called if this module is enabled.
     */
    void _update() {
        if (enabled) {
            update();
        }
    }

    /**
     * Override this with any init tasks. Called when the {@link Dispatcher#initialize_modules()} method is called.
     * This method should be considered second-level initialization. First-level initialization should be placed
     * in the constructor of each module.
     */
    protected void init() {}

    /**
     * Override this with any tasks that need to be updated frequently. Called every time {@link Dispatcher#run()} is called.
     */
    protected void update() {}

    /**
     * Override this with any tasks that need to be performed when this module enters the enabled state.
     * Note this is not called when the module initializes into the enabled state. If you want this method
     * to be called on initialization, call it from the init() method.
     */
    protected void enabled() {}

    /**
     * Override this with any tasks that need to be performed when this module enters the disabled state.
     */
    protected void disabled() {}

    // Enforce subclasses to implement meaningful string representations
    @Override
    public abstract String toString();
}
