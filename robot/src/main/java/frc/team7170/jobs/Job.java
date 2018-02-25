package frc.team7170.jobs;

import java.util.Arrays;
import java.util.Map;
import java.util.HashSet;


public abstract class Job {

    HashSet<Module> requirements = new HashSet<>();

    private boolean running = false;
    private boolean interruptable = false;

    protected final boolean requires(Module...mods) {
        if (!running) {
            requirements.addAll(Arrays.asList(mods));
        }
        return !running;
    }

    void start() {
        running = true;
        init();
    }

    void cancel() {
        if (interruptable) {
            running = false;
            interrupted();
        }
    }

    boolean _update() {
        if (running) {
            update();
            return is_finished();
        }
        return true;
    }

    void _end() {
        running = false;
        end();
    }

    public final boolean is_running() {
        return running;
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
