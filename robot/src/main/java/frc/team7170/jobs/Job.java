package frc.team7170.jobs;

import java.util.HashSet;


public abstract class Job {

    private HashSet<Module> requirements = new HashSet<>();
    private boolean running = false;
    private boolean started = false;
    private boolean interruptable = false;
    private HashSet<Module> disabled_parents = new HashSet<>();

    protected final boolean requires(Module...mods) {
        if (!running) {
            for (Module mod: mods) {
                requirements.add(mod);
                if (!mod.get_enabled()) {
                    disabled_parents.add(mod);
                }
            }
        }
        return !running;
    }

    void start() {
        if (!running & !started) {
            running = true;
            started = true;
            init();
        }
    }

    boolean cancel(boolean override) {
        if (running & (interruptable | override)) {
            running = false;
            interrupted();
            return true;
        }
        return false;
    }

    boolean _update() {
        if (is_updating()) {
            update();
        }
        if (is_finished()) {
            running = false;
            end();
            return true;
        }
        return false;
    }

    public final boolean is_running() {
        return running;
    }

    public final boolean is_updating() {
        return running & disabled_parents.isEmpty();
    }

    public final boolean is_interruptable() {
        return interruptable;
    }

    protected final boolean set_interruptable(boolean interruptable) {
        if (!running) {
            this.interruptable = interruptable;
        }
        return !running;
    }

    public boolean does_require(Module mod) {
        return requirements.contains(mod);
    }

    protected HashSet<Module> get_requirements() {
        return requirements;
    }

    void signal_parent_state_change(Module mod, boolean disabled) {
        if (disabled) {
            disabled_parents.add(mod);
        } else {
            disabled_parents.remove(mod);
        }
    }

    protected void init() {}

    protected abstract void update();

    protected abstract boolean is_finished();

    protected void end() {}

    protected void interrupted() {
        end();
    }

    // Enforce subclasses to implement meaningful string representations
    @Override
    public abstract String toString();

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

    public static Job mesh(Job ...jobs) {
        return null;  // TODO
    }
}
