package org.millenaire.entities.jobs;

import org.millenaire.entities.Citizen;

/**
 * Base class for all citizen jobs.
 * 
 * Jobs define what a citizen does during their work day.
 * Inspired by MineColonies job system but implemented from scratch.
 */
public abstract class Job {
    protected final Citizen citizen;

    public Job(Citizen citizen) {
        this.citizen = citizen;
    }

    /**
     * Called every tick when citizen is working
     */
    public abstract void tick();

    /**
     * Get the job type identifier
     */
    public abstract String getType();

    /**
     * Add AI goals specific to this job
     */
    public abstract void addGoals(Citizen citizen);

    /**
     * Remove AI goals when job is removed
     */
    public abstract void removeGoals(Citizen citizen);

    /**
     * Check if citizen can perform this job
     */
    public boolean canPerformJob() {
        return true;
    }
}
