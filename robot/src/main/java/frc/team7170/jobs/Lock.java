package frc.team7170.jobs;

public class Lock {

    private boolean locked = false;
    private Job curr_job = null;

    Lock() {}

    /**
     * Attempt to claim the lock on this module and assign it to the given {@link Job}.
     * @param job The job that claims this lock.
     * @return Whether the operation was successful (returns false if the lock was already engaged when this function is called)
     */
    public boolean claim(Job job) {
        if (!locked) {
            locked = true;
            curr_job = job;
        }
        return locked;
    }

    void free() {
        locked = false;
        curr_job = null;
    }

    public boolean is_locked() {
        return locked;
    }

    public Job get_curr_job() {
        return curr_job;
    }
}
