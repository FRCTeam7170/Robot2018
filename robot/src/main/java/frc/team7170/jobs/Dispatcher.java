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
 * TODO Fix spaghetti, i.e. ALL the code involving checking if a job should run etc. should be here, not also in Job and Module
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

    private final ArrayList<Job> queued_jobs = new ArrayList<>();
    private final HashSet<Job> running_jobs = new HashSet<>();
    private final HashMap<Module, Boolean> modules = new HashMap<>();  // Module-Boolean pairs where the boolean represents if the module is locked.

    private boolean jobs_updated = false;  // State variable to indicate whether a running job has terminated and thus queued jobs should be queried (see run()).

    /**
     * Must be called by each class that inherits from Module, most likely in the constructor (therefore each module should be a singleton).
     * @param mod The module to register
     */
    public synchronized void register_module(Module mod) {
        modules.putIfAbsent(mod, false);
    }

    /**
     * Run a {@link Job} if its required {@link Module}s are free. Otherwise queue the job and run it
     * when the module locks are freed. Note that the priority of the jobs is the order they are added in.
     * @param job The job to run/queue.
     */
    public synchronized void add_job(Job job, Module ...mods) {
        job.requires(mods);  // TODO: temp
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
    public synchronized void run() {
        // Update each module
        modules.forEach((Module mod, Boolean locked) -> {
            mod._update();
            /*
            if (mod.get_current_job() == null && mod.get_default_job() != null) {
                // Start the default job without claiming the lock so new jobs with this module as a requirement can override it
                running_jobs.add(mod.get_default_job());
                mod.get_default_job().start();  // This will only have any effect if the job has not started yet
            } else {
                // Remove the default job if the module has a current job
                // Doing this every iteration isn't ideal, but this call should be pretty cheap and I can't think of a better method without a lot of work
                running_jobs.remove(mod.get_default_job());
            }
            */
        });

        // Update each job and remove it if it's finished
        for (Iterator<Job> iter = running_jobs.iterator(); iter.hasNext();) {
            Job job = iter.next();
            if (job._update()) {  // returns true if the job is finished
                jobs_updated = true;
                free_module_locks(job);
                iter.remove();
            }
        }

        // Run new jobs if required module locks are free
        if (jobs_updated) {  // Only iterate through the queued jobs if a change to the running ones has occurred
            for (ListIterator<Job> iter = queued_jobs.listIterator(); iter.hasNext();) {
                Job job = iter.next();
                if (can_run_job(job)) {  // If the necessary module locks are free, start the highest priority (dictated by order added to list) job
                    start_job(job);
                    iter.remove();
                }
            }
        }

        jobs_updated = false;  // Reset the jobs_updated so we continue to iterate through the queued_jobs only if a running job has terminated
    }

    /**
     * Called internally to claim {@link Module} locks, populate the running_jobs list, and start a {@link Job}.
     * @param job The job to start.
     */
    private synchronized void start_job(Job job) {
        // Loop through the job's requirements and claim its required modules
        for (Module mod : job.get_requirements()) {
            if (!mod.claim_lock(job)) {
                // If the dispatcher handles jobs & modules correctly, this should never happen.
                // TODO: this is moderately gross.
                throw new RuntimeException("Job " + job + " attempted to claim Module lock from " + mod + " but Job " + mod.get_current_job() + " owns the lock!");
            }
            modules.replace(mod, true);
        }
        LOGGER.fine("Starting job: " + job.toString());
        running_jobs.add(job);
        job.start();
    }

    /**
     * Essentially the opposite of start_job(), except, because a peaceful termination of a {@link Job} ends itself,
     * we only free {@link Module} locks. The job must also be removed from the {@link Dispatcher#running_jobs} set
     * (this is not done inside this function because if the running_jobs set is begin iterated over when this is
     * called, a {@link ConcurrentModificationException} results.
     * @param job The job to free locks from.
     */
    private synchronized void free_module_locks(Job job) {
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
    private synchronized boolean can_run_job(Job job) {
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
    public synchronized boolean cancel_job(Job job, boolean override) {
        if (job.cancel(override)) {
            LOGGER.fine("Cancelling job: "+job.toString());
            free_module_locks(job);
            running_jobs.remove(job);
            return true;
        }
        return false;
    }

    /**
     * Forcefully {@link Job#cancel(boolean)} every job.
     */
    public synchronized void cancel_all() {
        LOGGER.fine("Cancelling all jobs.");
        queued_jobs.clear();  // Clear the queued jobs.
        for (Iterator<Job> iter = running_jobs.iterator(); iter.hasNext();) {
            Job job = iter.next();
            free_module_locks(job);
            job.cancel(true);
            iter.remove();
        }
    }

    @SuppressWarnings("unused")
    @Transmitter(poll_rate = TransmitFrequency.SLOW, value = {
            "O_RUNNING_JOBS_NT",
            "O_QUEUED_JOBS_NT"
    })
    public void transmitter_slow(NetworkTableEntry entry) {
        switch (entry.getName()) {
            case "O_RUNNING_JOBS_NT":
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
            case "O_QUEUED_JOBS_NT":
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
