package frc.team7170.jobs;


/**
 * Convert given {@link Runnable} objects and stall time into a {@link Job} with the given requirements.
 * Can be used as just a delay, or just perform a simply task not warranting the need of an entire new Job type.
 */
public class JRunnable extends Job {

    private int stall_ms;
    private long start_time;
    private Runnable rinit;
    private Runnable rperiodic;
    private Runnable rend;

    public JRunnable(Runnable init, Runnable periodic, Runnable end, int stall_ms, Module ...mods) {
        rinit = init;
        rperiodic = periodic;
        rend = end;
        this.stall_ms = stall_ms;
        requires(mods);
    }

    public JRunnable(Runnable runnable, Module ...mods) {
        this(runnable, null, null, 0, mods);
    }

    public JRunnable(int stall_ms, Module ...mods) {
        this(null, null, null, stall_ms, mods);
    }

    @Override
    protected void init() {
        start_time = System.currentTimeMillis();
        if (rinit != null) {
            rinit.run();
        }
    }

    @Override
    protected void update() {
        if (rperiodic != null) {
            rperiodic.run();
        }
    }

    @Override
    protected boolean is_finished() {
        return System.currentTimeMillis() >= start_time + stall_ms;
    }

    @Override
    protected void interrupted() {
        end();
    }

    @Override
    protected void end() {
        if (rend != null) {
            rend.run();
        }
    }

    @Override
    public String toString() {
        return "JRunnable(stall_time: "+stall_ms+")";
    }
}
