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
        // Update each module
        modules.forEach((Module key, Boolean value) -> key.update());

        boolean job_removed = false;
        for (Job job: running_jobs) {
            if (job._update()) {
                job._end();  // TODO: Interrupting jobs, this doesnt work
                running_jobs.remove(job);
                job_removed = true;
                for (Module mod: job.requirements) {
                    mod.free_lock();
                    modules.replace(mod, false);
                }
            }
        }

        if (job_removed) {
            for (Job job: queued_jobs) {
                if (can_run_job(job)) {
                    queued_jobs.remove(job);
                    start_job(job);
                }
            }
        }
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

    public boolean can_run_job(Job job) {
        return !Job.conflicts(job, modules);
    }

    public void initialize_modules() {
        modules.forEach((Module key, Boolean value) -> key.init());
    }
}
