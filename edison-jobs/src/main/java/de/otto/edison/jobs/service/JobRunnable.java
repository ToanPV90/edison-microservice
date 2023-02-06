package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;

/**
 * The executable part of a Job that is executing background tasks in Edison microservices.
 *
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobRunnable {

    /**
     * Returns the definition of the job, specifying when a job should be executed.
     *
     * @return JobDefinition
     */
    JobDefinition getJobDefinition();

    /**
     * Executes the background task of the job and updates the JobInfo during execution.
     *
     * @return false, if the job was skipped without doing anything, true otherwise
     */
    default boolean execute() {
        return true;
    }

}
