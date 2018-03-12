package frc.team7170.jobs;

import edu.wpi.first.networktables.NetworkTableEntry;
import frc.team7170.comm.Communicator;
import frc.team7170.comm.TransmitFrequency;
import frc.team7170.comm.Transmitter;
import java.util.*;
import java.util.logging.Logger;


/**
 * This class controls dispatching {@link Job}s to {@link Module}s and ensures that no module will be assigned more than one
 * job to avoid motor conflicts, etcetera. This class also manages the initialization and updating of each module and job.
 *
 * Quick guidelines:
 * Call register_module(Module) in the constructor of each module.
 * Call add_job(Job) to queue/run each job.
 * Call run() regularly in the robot main loop.
 */
public class Dispatcher implements Communicator {

    private final static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

    private static Dispatcher instance = new Dispatcher();  // Singleton
    public static Dispatcher get_instance() {
        return instance;
    }
    private Dispatcher() {
        LOGGER.info("Initializing dispatcher.");
        register_comm();
    }

    private ArrayList<Job> queued_jobs = new ArrayList<>();
    private HashSet<Job> running_jobs = new HashSet<>();
    private HashMap<Module, Boolean> modules = new HashMap<>();  // Module-Boolean pairs where the boolean represents if the module is locked.

    private boolean jobs_updated = false;  // State variable to indicate whether a running job has terminated and thus queued jobs should be queried (see run()).

    /**
     * Must be called by each class that inherits from Module, most likely in the constructor (therefore each module should be a singleton).
     * @param mod The module to register
     */
    public void register_module(Module mod) {
        modules.putIfAbsent(mod, false);
    }

    /**
     * Run a {@link Job} if its required {@link Module}s are free. Otherwise queue the job and run it
     * when the module locks are freed. Note that the priority of the jobs is the order they are added in.
     * @param job The job to run/queue
     */
    public void add_job(Job job) {
        if (can_run_job(job)) {
            start_job(job);
        } else {
            queued_jobs.add(job);
        }
    }

    /**
     * Updates every registered {@link Module}, updates running {@link Job}, and runs queued jobs if module locks are free.
     * This should be called regularly in the robot main loop.
     */
    public void run() {
        // Update each module and run defaults if locks are free
        modules.forEach((Module mod, Boolean locked) -> {
            mod._update();
            if (mod.get_current_job() == null && mod.get_default_job() != null) {
                // Start the default job without claiming the lock so new jobs with this module as a requirement can override it
                running_jobs.add(mod.get_default_job());
                mod.get_default_job().start();  // This will only have any effect if the job has not started yet
            } else {
                // Remove the default job if the module has a current job
                // Doing this every iteration isn't ideal, but this call should be pretty cheap and I can't think of a better method without a lot of work
                running_jobs.remove(mod.get_default_job());
            }
        });

        // Update each job and remove it if it's finished
        for (Job job: running_jobs) {
            if (job._update()) {  // returns true if the job is finished
                jobs_updated = true;
                free_module_locks(job);
            }
        }

        // Run new jobs if required module locks are free
        if (jobs_updated) {  // Only iterate through the queued jobs if a change to the running ones has occurred
            for (Job job: queued_jobs) {
                if (can_run_job(job)) {  // If the necessary module locks are free, start the highest priority (dictated by order added to list) job
                    queued_jobs.remove(job);
                    start_job(job);
                }
            }
        }

        jobs_updated = false;  // Reset the jobs_updated so we continue to iterate through the queued_jobs only if a running job has terminated
    }

    /**
     * Called internally to claim {@link Module} locks, populate the running_jobs list, and start a {@link Job}.
     * @param job The job to start
     */
    private void start_job(Job job) {
        // Loop through the job's requirements and claim its required modules
        for (Module mod: job.get_requirements()) {
            if (!mod.claim_lock(job)) {
                // If the dispatcher handles jobs & modules correctly, this should never happen.
                throw new RuntimeException("Job " + job + " attempted to claim Module lock from " + mod + " but Job " + mod.get_current_job() + " owns the lock!");
            }
            modules.replace(mod, true);
        }
        running_jobs.add(job);
        job.start();
    }

    /**
     * Essentially the opposite of start_job(), except, because a peaceful termination of a {@link Job} ends itself,
     * we only free {@link Module} locks and remove the job from the running_jobs set.
     * @param job The job to free locks from.
     */
    private void free_module_locks(Job job) {
        running_jobs.remove(job);
        for (Module mod: job.get_requirements()) {
            mod.free_lock();
            modules.replace(mod, false);
        }
    }

    /**
     * Checks if the given {@link Job} can be ran or not by iterating its requirements and comparing to the state of each {@link Module}.
     * @param job The job to check for conflicts on.
     * @return Whether or not the job can be ran.
     */
    private boolean can_run_job(Job job) {
        for (Module mod: job.get_requirements()) {
            if (modules.get(mod)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Forcefully {@link Job#cancel(boolean)} the given job.
     * @param job The job to cancel.
     * @param override Whether or not to override the job's interruptable attribute.
     * @return Whether or not the cancellation was successful. This will always be true if override is true.
     */
    public boolean cancel_job(Job job, boolean override) {
        return job.cancel(override);
    }

    /**
     * Forcefully {@link Job#cancel(boolean)} every job.
     */
    public void cancel_all() {
        queued_jobs.clear();  // Clear the queued jobs.
        for (Job job: running_jobs) {  // Clear the running jobs.
            job.cancel(true);
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.SLOW, value = {
            "O_RUNNING_JOBS_S",
            "O_QUEUED_JOBS_S"
    })
    public void transmitter_slow(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_RUNNING_JOBS_S":
                String[] running_strs = new String[running_jobs.size()];
                Iterator running_iter = running_jobs.iterator();
                for (int i = 0; i < running_jobs.size(); i++) {
                    if (!running_iter.hasNext()) {
                        break;
                    }
                    running_strs[i] = running_iter.next().toString();
                }
                entry.setStringArray(running_strs);
                break;
            case "O_QUEUED_JOBS_S":
                String[] queued_strs = new String[queued_jobs.size()];
                Iterator queued_iter = queued_jobs.iterator();
                for (int i = 0; i < queued_jobs.size(); i++) {
                    if (!queued_iter.hasNext()) {
                        break;
                    }
                    queued_strs[i] = queued_iter.next().toString();
                }
                entry.setStringArray(queued_strs);
                break;
        }
    }
}
