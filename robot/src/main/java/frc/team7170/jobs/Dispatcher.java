package frc.team7170.jobs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.logging.Logger;


public class Dispatcher {

    private final static Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

    private Dispatcher instance = new Dispatcher();  // Singleton

    public Dispatcher get_instance() {
        return instance;
    }

    private Dispatcher() {}

    private ArrayList<Job> queued_jobs = new ArrayList<>();
    private HashSet<Job> running_jobs = new HashSet<>();
    private HashMap<Module, Boolean> modules = new HashMap<>();

    private boolean jobs_updated = false;

    public void register_module(Module mod) {
        modules.putIfAbsent(mod, false);
    }

    public void add_job(Job job) {
        if (can_run_job(job)) {
            start_job(job);
        } else {
            queued_jobs.add(job);
        }
    }

    public void run() {
        // Update each job and remove it if it's finished
        for (Job job: running_jobs) {
            if (job._update()) {
                stop_job(job, true);
            }
        }

        // Run new jobs if required module locks are free
        if (jobs_updated) {
            for (Job job: queued_jobs) {
                if (can_run_job(job)) {
                    queued_jobs.remove(job);
                    start_job(job);
                }
            }
        }

        // Update each module and run defaults if free
        modules.forEach((Module mod, Boolean locked) -> {
            mod._update();
            if (!locked) {
                mod.get_current_job()._update();  // TODO: Incorporate into main running_jobs set and remove it if a non-default job wants to run
            }
        });

        jobs_updated = false;
    }

    private void start_job(Job job) {
        for (Module mod: job.requirements) {
            if (!mod.claim_lock(job)) {
                // If the dispatcher handles jobs & modules correctly, this should never happen.
                throw new RuntimeException("Job "+job+" attempted to claim Module lock from "+mod+" but Job "+mod.get_current_job()+" owns the lock!");
            }
            modules.replace(mod, true);
        }
        running_jobs.add(job);
        job.start();
    }

    private void stop_job(Job job, boolean peaceful) {
        jobs_updated = true;
        job.stop(peaceful);
        running_jobs.remove(job);
        for (Module mod: job.requirements) {
            mod.free_lock();
            modules.replace(mod, false);
        }
    }

    public boolean can_run_job(Job job) {
        return !Job.conflicts(job, modules);
    }

    public void initialize_modules() {
        modules.forEach((Module key, Boolean value) -> key.init());
    }

    public boolean cancel_job(Job job, boolean override) {
        if (job.is_interruptable() | override) {
            stop_job(job, false);
            return true;
        }
        return false;
    }

    public void cancel_all() {
        queued_jobs.clear();
        for (Job job: running_jobs) {
            stop_job(job, false);
        }
    }
}
