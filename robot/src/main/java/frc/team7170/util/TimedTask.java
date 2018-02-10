package frc.team7170.util;


/**
 * Class to call a function every after n milliseconds.
 * Calls to run() must be often to ensure that the timing is close to exact.
 */
public class TimedTask {

    private final int delay;
    private final Runnable func;
    private boolean running = false;
    private long time;

    public TimedTask(Runnable func, int delay) {
        this(func, delay, true);
    }

    public TimedTask(Runnable func, int delay, boolean start) {
        this.func = func;
        this.delay = delay;
        if (start) start();
    }

    public void start() {
        func.run();
        running = true;
        time = System.currentTimeMillis();
    }

    public void stop() {
        running = false;
    }

    public void run() {
        if (this.running & System.currentTimeMillis() > time + delay) {
            func.run();
            time = System.currentTimeMillis();
        }
    }
}
