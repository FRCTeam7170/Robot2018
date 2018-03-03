package frc.team7170.util;


/**
 * Class to call a function after n milliseconds. Calls to run() must be often (i.e. put in loop)
 * to ensure that the timing is close to exact--although, be warned, it never will be.
 * Consider other timing methods in the java libraries for more exactly timing.
 */
public class TimedTask {

    private final int delay;
    private final Runnable func;
    private boolean running = false;
    private long time;

    /**
     * Alternative constructor to start timer on init by default.
     * @param func Some Runnable (no args or return values) to run: usually a lambda expression.
     * @param delay Delay in milliseconds.
     */
    public TimedTask(Runnable func, int delay) {
        this(func, delay, true);
    }

    /**
     * @param func Some Runnable (no args or return values) to run: usually a lambda expression.
     * @param delay Delay in milliseconds.
     * @param start Whether to start timer on initialization or not.
     */
    public TimedTask(Runnable func, int delay, boolean start) {
        this.func = func;
        this.delay = delay;
        if (start) start();
    }

    /**
     * Starts the timer. Note that the function is run once as soon as you start if start param is true.
     */
    public void start() {
        func.run();
        running = true;
        time = System.currentTimeMillis();
    }

    /**
     * Stops the given function from executing even if calls to run() persist.
     * Call start() to restart execution.
     */
    public void stop() {
        running = false;
    }

    /**
     * Checks if delay milliseconds has passed since last execution and runs the func if true.
     * Call this regularly in a loop.
     */
    public void run() {
        if (this.running && System.currentTimeMillis() >= time + delay) {
            func.run();
            time = System.currentTimeMillis();
        }
    }
}
