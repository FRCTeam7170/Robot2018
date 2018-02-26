package frc.team7170.jobs;

import java.util.Map;
import java.util.HashSet;


public abstract class Job {

    HashSet<Module> requirements = new HashSet<>();

    private boolean running = false;
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
        if (!running) {
            running = true;
            init();
        }
    }

    void stop(boolean peaceful) {
        if (running) {
            if (peaceful) {
                running = false;
                end();
            } else {
                running = false;
                interrupted();
            }
        }
    }

    boolean _update() {
        if (is_running()) {
            update();
        }
        return is_finished();
    }

    public final boolean is_running() {
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

    void signal_parent_state_change(Module mod, boolean disabled) {
        if (disabled) {
            disabled_parents.add(mod);
        } else {
            disabled_parents.remove(mod);
        }
    }

    protected abstract void init();

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

    public static boolean conflicts(Job job, Map<Module, Boolean> mods) {
        for (Module mod: job.requirements) {
            if (mods.get(mod)) {
                return true;
            }
        }
        return false;
    }

    public static Job mesh(Job ...jobs) {
        return null;  // TODO
    }
}
