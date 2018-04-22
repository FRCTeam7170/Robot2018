package frc.team7170.jobs;

import java.util.HashSet;


/**
 * Defines a job: an operation that works on zero or more {@link Module}. Only one job can work on each user-defined module
 * at a time (this is enforced by the {@link Dispatcher}).
 *
 * As an example, if you made a drive base module/subsystem, you may want to define a job that turns the bot theta degrees.
 * It might use encoder values to set motor speeds. This job can then be used multiple times whenever autonomously turning
 * the robot is desired.
 *
 * Quick guidelines:
 * Call requires with all the modules this job uses otherwise two jobs might try to simultaneously change motor speeds on the same motors, for example.
 * Override the init, update, is_finished, end, and interrupted methods appropriately.
 * All classes which inherit from Job should be prefixed with "J..."
 */
public abstract class Job {

    private boolean running = false;  // State variable to indicate if the job is in run state (start() has been called).
    private boolean started = false;  // State variable to indicate if the job has been started (prohibits starting the job twice).
    private boolean interruptable = false;  // If this job is interruptable
    private HashSet<Module> disabled_parents = new HashSet<>();  // Set of currently disabled parents to determine if this job should update
    private HashSet<Module> requirements = new HashSet<>();  // Set of required modules for this job

    /**
     * Register a {@link Module} or modules that this job requires. This ensures that upon the execution of this job,
     * the necessary module locks will be claimed. It is recommended that this method be called appropriately
     * in the constructor of each job object.
     * @param mods Modules to set as required.
     * @return Whether the module was successfully added or not.
     */
    protected final boolean requires(Module...mods) {
        if (!running) {  // Disallow changing the required jobs after this job has started running
            for (Module mod: mods) {
                requirements.add(mod);
                if (!mod.get_enabled()) {  // If one of the modules being added is disabled, append it to the disabled_parents set
                    disabled_parents.add(mod);
                }
            }
        }
        return !running;
    }

    /**
     * Method called from {@link Dispatcher} to start a job.
     */
    void start() {
        // Disallow running this method if it's already been started. A consequence of this is that jobs are not recyclable.
        // If is_finished() == true, it's expected that the job is to be garbage collected; it no longer has any purpose.
        if (!running && !started) {
            running = true;
            started = true;  // This second state variable is required because running is set back to false when the job terminates.
            init();  // Call the user-defined init method.
        }
    }

    /**
     * Cancel the job (not peaceful).
     * @param override Whether or not to override this jobs interruptability.
     * @return If cancelling the job was successful.
     */
    boolean cancel(boolean override) {
        if (running && (interruptable || override)) {
            running = false;
            interrupted();  // Forced termination
            return true;
        }
        return false;
    }

    /**
     * Called from within {@link Dispatcher}. Updates the job and peacefully terminates if the job is finished.
     * @return If the job finished peacefully.
     */
    boolean _update() {
        if (is_updating()) {  // Only update if running and all required modules are enabled.
            update();
        }
        if (is_finished()) {
            running = false;
            end();  // Peaceful termination
            return true;
        }
        return false;
    }

    /**
     * @return If this job is running.
     */
    public final boolean is_running() {
        return running;
    }

    /**
     * @return If this job is updating (more restrictive than is_running() ).
     */
    public final boolean is_updating() {
        return running && disabled_parents.isEmpty();
    }

    /**
     * @return If this job is interruptable.
     */
    public final boolean is_interruptable() {
        return interruptable;
    }

    /**
     * Set whether this job can be interrupted forcefully before is_finished() returns true.
     * @param interruptable If this job is to be interruptable or not.
     * @return If changing the interruptability was successful.
     */
    protected final boolean set_interruptable(boolean interruptable) {
        if (!running) {  // Disallow changing this while the job is running.
            this.interruptable = interruptable;
        }
        return !running;
    }

    /**
     * Check if this job requires the given {@link Module}.
     * @param mod The module to check for dependency on.
     * @return Whether this job is dependent on that module or not.
     */
    public boolean does_require(Module mod) {
        return requirements.contains(mod);
    }

    /**
     * @return A set of all the required {@link Module}s for this job.
     */
    protected HashSet<Module> get_requirements() {
        return requirements;
    }

    /**
     * Called from within {@link Module}s to signal when they changed from enabled to disabled or vice versa.
     * This makes the job only run when all its parents are enabled.
     * @param mod The module having its state changed.
     * @param disabled True if the module is being disabled.
     */
    void signal_parent_state_change(Module mod, boolean disabled) {
        if (disabled) {
            disabled_parents.add(mod);
        } else {
            disabled_parents.remove(mod);
        }
    }

    /**
     * Override this with any init tasks.
     */
    protected void init() {}

    /**
     * Override this with any tasks that need to be updated frequently. Called every time {@link Dispatcher#run()} is called.
     * This is required.
     */
    protected abstract void update();

    /**
     * Override this to return a boolean of whether this job is done or not.
     * If you wish to make a job never end--this may be useful for a default job in a module, for example--
     * simply set this to always return false.
     * This is required.
     * @return If the job is finished or not.
     */
    protected abstract boolean is_finished();

    /**
     * Override this with any tasks that need to be called when this job terminates peacefully (free resources, for example).
     */
    protected void end() {}

    /**
     * Override this with any tasks that need to be called when this job is forced to terminate early.
     */
    protected void interrupted() {
        end();  // Simply call end() by default.
    }

    // Enforce subclasses to implement meaningful(ish) string representations
    @Override
    public abstract String toString();

    /**
     * Check for conflicting requirements between two or more given jobs.
     * @param jobs Jobs to check for conflicts on.
     * @return  If there are any conflicts (true == yes).
     */
    public static boolean conflicts(Job ...jobs) {
        HashSet<Module> req = new HashSet<>();
        for (Job job: jobs) {
            for (Module mod: job.requirements) {
                if (!req.add(mod)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Group together a set of jobs to be ran in parallel. This DOES NOT disallow conflicting requirements so this must
     * be used responsibly.
     * Each meshed job starts at the same time as the others, but may terminate early depending on its own
     * {@link Job#is_finished()} conditions. Cancelling a job mesh terminates all the meshed jobs at the same time. All
     * meshed jobs will halt if any of the required modules for any of the meshed jobs becomes disabled.
     * @param jobs Set of jobs to be ran in parallel.
     * @param interruptable Whether the meshed job is interruptable or not.
     * @return The meshed job.
     */
    public static Job mesh(HashSet<Job> jobs, boolean interruptable) {
        Job meshed = new Job() {
            @Override
            void start() {
                super.start();
                for (Job job : jobs) {
                    job.start();
                }
            }

            @Override
            boolean cancel(boolean override) {
                if (super.cancel(override)) {
                    for (Job job : jobs) {
                        job.cancel(true);
                    }
                    return true;
                }
                return false;
            }

            @Override
            boolean _update() {
                if (is_updating()) {
                    for (Job job : jobs) {
                        job._update();
                    }
                }
                return super._update();
            }

            @Override
            protected void init() {}

            @Override
            protected void update() {}

            @Override
            protected boolean is_finished() {
                for (Job job : jobs) {
                    if (!job.is_finished()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            protected void end() {}

            @Override
            protected void interrupted() {}

            @Override
            public String toString() {
                return "Meshed job group.";
            }
        };
        for (Job job : jobs) {
            meshed.requires((Module[]) job.requirements.toArray());
        }
        meshed.set_interruptable(interruptable);
        return meshed;
    }
}
