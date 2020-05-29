package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.status.domain.Link;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.status.domain.Link.link;
import static java.time.format.DateTimeFormatter.*;
import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;
import static java.util.Arrays.asList;

public class JobRepresentation {

    static final ZoneId UTC_ZONE = ZoneId.of("Z");

    private final JobInfo job;
    private final String baseUri;
    private final boolean humanReadable;
    private final JobMeta jobMeta;
    private final String edisonManagementBasePath;

    private JobRepresentation(final JobInfo job,
                              final JobMeta jobMeta,
                              final boolean humanReadable,
                              final String baseUri,
                              final String edisonManagementBasePath) {
        this.job = job;
        this.humanReadable = humanReadable;
        this.baseUri = baseUri;
        this.jobMeta = jobMeta;
        this.edisonManagementBasePath = edisonManagementBasePath;
    }

    public static JobRepresentation representationOf(final JobInfo job,
                                                     final JobMeta jobMeta,
                                                     final boolean humanReadable,
                                                     final String baseUri,
                                                     final String edisonManagementBasePath) {
        return new JobRepresentation(job, jobMeta, humanReadable, baseUri, edisonManagementBasePath);
    }

    public String getJobUri() {
        return String.format("%s%s/jobs/%s", baseUri, edisonManagementBasePath, job.getJobId());
    }

    public String getJobType() {
        return job.getJobType();
    }

    public String getStatus() {
        return job.getStatus().name();
    }

    public String getState() {
        return job.isStopped() ? "Stopped" : "Running";
    }

    public String getStarted() {
        OffsetDateTime started = job.getStarted();
        return formatDateTime(started);
    }

    public String getStartedIso() {
        return ISO_DATE_TIME.format(job.getStarted().atZoneSameInstant(UTC_ZONE));
    }

    public String getStopped() {
        return job.isStopped()
                ? formatTime(job.getStopped().get())
                : "";
    }

    public String getStoppedIso() {
        return job.isStopped()
                ? ISO_DATE_TIME.format(job.getStopped().get().atZoneSameInstant(UTC_ZONE)) : "";
    }

    public String getRuntime() {
        return job.isStopped()
                ? formatRuntime(job.getStarted(), job.getStopped().get())
                : formatRuntime(job.getStarted(), OffsetDateTime.now());
    }

    public String getLastUpdated() {
        return formatTime(job.getLastUpdated());
    }

    public String getLastUpdatedIso() {
        return ISO_DATE_TIME.format(job.getLastUpdated().atZoneSameInstant(UTC_ZONE));
    }

    public String getHostname() {
        return job.getHostname();
    }

    public boolean getIsDisabled() {
        return jobMeta != null && jobMeta.isDisabled();
    }

    public String getComment() {
        return jobMeta != null ? jobMeta.getDisabledComment() : "";
    }

    public String getId() {
        return job.getJobId();
    }

    public List<String> getMessages() {
        return job.getMessages().stream().map((jobMessage) ->
                "[" + formatTime(jobMessage.getTimestamp()) + "] [" + jobMessage.getLevel().getKey() + "] " + jobMessage.getMessage()
        ).collect(Collectors.toList());
    }

    public List<JobMessage> getRawMessages() {
        return job.getMessages();
    }

    public List<Link> getLinks() {
        final String jobUri = String.format("%s%s/jobs/%s", baseUri, edisonManagementBasePath, job.getJobId());
        return asList(
                link("self", jobUri, "Self"),
                link("http://github.com/otto-de/edison/link-relations/job/definition", String.format("%s%s/jobdefinitions/%s", baseUri, edisonManagementBasePath, job.getJobType()), "Job Definition"),
                link("collection", jobUri.substring(0, jobUri.lastIndexOf("/")), "All Jobs"),
                link("collection/" + getJobType(), jobUri.substring(0, jobUri.lastIndexOf("/")) + "?type=" + getJobType(), "All " + getJobType() + " Jobs")
        );
    }

    private String formatRuntime(OffsetDateTime started, OffsetDateTime stopped) {
        Duration duration = Duration.between(started, stopped);

        if (duration.toHours() >= 24) {
            return "> 24h";
        }

        LocalTime dateTime = LocalTime.ofSecondOfDay(duration.getSeconds());
        return humanReadable
                ? ofPattern("HH:mm:ss").format(dateTime)
                : ofPattern("HH:mm:ss").format(dateTime);
    }

    private String formatDateTime(final OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return humanReadable
                    ? ofLocalizedDateTime(SHORT, MEDIUM).format(dateTime)
                    : ISO_OFFSET_DATE_TIME.format(dateTime);
        }
    }

    private String formatTime(final OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return humanReadable
                    ? ofLocalizedTime(MEDIUM).format(dateTime)
                    : ISO_OFFSET_DATE_TIME.format(dateTime);
        }
    }

    private String formatTimeIso(final OffsetDateTime dateTime) {
        return ISO_DATE_TIME.format(dateTime.atZoneSameInstant(UTC_ZONE));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobRepresentation that = (JobRepresentation) o;

        if (job != null ? !job.equals(that.job) : that.job != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return job != null ? job.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "JobRepresentation{" +
                "job=" + job +
                '}';
    }
}
