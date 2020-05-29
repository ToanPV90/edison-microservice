package de.otto.edison.jobs.configuration;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.jobs.definition.DefaultJobDefinition;
import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.service.JobDefinitionService;
import de.otto.edison.jobs.status.JobStatusCalculator;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.CompositeStatusDetailIndicator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.status.domain.Status.OK;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JobsConfigurationTest {

    private EdisonApplicationProperties properties;
    private JobsConfiguration testee;

    @BeforeEach
    public void setUp() throws Exception {
        properties = new EdisonApplicationProperties();
        testee = new JobsConfiguration(new JobsProperties(), properties);
    }

    @Test
    public void shouldIndicateOkIfNoJobDefinitionsAvailable() {
        // given
        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        final JobDefinitionService noJobDefinitions = mock(JobDefinitionService.class);
        when(noJobDefinitions.getJobDefinitions()).thenReturn(emptyList());

        // when
        final StatusDetail statusDetail = testee.jobStatusDetailIndicator(
                noJobDefinitions,
                singletonList(defaultCalculator)).statusDetails()
                .get(0);
        // then
        assertThat(statusDetail.getStatus(), is(OK));
        assertThat(statusDetail.getName(), is("Jobs"));
        assertThat(statusDetail.getMessage(), is("No job definitions configured in application."));
    }

    @Test
    public void shouldConstructCompositeStatusDetailIndicator() {
        // given
        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        // when
        final StatusDetailIndicator indicator = testee.jobStatusDetailIndicator(
                someJobDefinitionService(),
                singletonList(defaultCalculator)
        );
        // then
        assertThat(indicator, is(instanceOf(CompositeStatusDetailIndicator.class)));
    }

    @Test
    public void shouldUseDefaultJobStatusDetailIndicator() {
        // given
        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        // when
        testee.jobStatusDetailIndicator(
                someJobDefinitionService(), singletonList(defaultCalculator)).statusDetails();

        // then
        verify(defaultCalculator).statusDetail(any(JobDefinition.class));
    }

    @Test
    public void shouldUseConfiguredJobStatusDetailIndicator() {
        // given
        final JobsProperties jobsProperties = new JobsProperties();
        jobsProperties.getStatus().setCalculator(new HashMap<String, String>(){{
            put("test", "errorOnLastJobFailed");
        }});

        testee = new JobsConfiguration(jobsProperties, properties);

        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        final JobStatusCalculator testCalculator = mock(JobStatusCalculator.class);
        when(testCalculator.getKey()).thenReturn("errorOnLastJobFailed");

        // when
        testee.jobStatusDetailIndicator(
                someJobDefinitionService(), asList(defaultCalculator, testCalculator)).statusDetails();

        // then
        verify(testCalculator).statusDetail(any(JobDefinition.class));
    }

    @Test
    public void shouldNormalizeJobTypesInConfiguredJobStatusDetailIndicators() {
        // given
        final JobsProperties jobsProperties = new JobsProperties();
        jobsProperties.getStatus().setCalculator(new HashMap<String, String>(){{
            put("soMe-TeSt job", "errorOnLastJobFailed");
        }});

        testee = new JobsConfiguration(jobsProperties, properties);

        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        final JobStatusCalculator testCalculator = mock(JobStatusCalculator.class);
        when(testCalculator.getKey()).thenReturn("errorOnLastJobFailed");

        final JobDefinitionService jobDefinitionService = mock(JobDefinitionService.class);
        when(jobDefinitionService.getJobDefinitions()).thenReturn(singletonList(
                someJobDefinition("Some Test Job"))
        );

        // when
        testee.jobStatusDetailIndicator(
                jobDefinitionService, asList(defaultCalculator, testCalculator)).statusDetails();

        // then
        verify(testCalculator).statusDetail(any(JobDefinition.class));
    }

    private JobDefinitionService someJobDefinitionService() {
        final JobDefinitionService jobDefinitionService = mock(JobDefinitionService.class);
        when(jobDefinitionService.getJobDefinitions()).thenReturn(singletonList(someJobDefinition("test")));
        return jobDefinitionService;
    }

    private DefaultJobDefinition someJobDefinition(final String jobType) {
        return fixedDelayJobDefinition(
                jobType,
                "test",
                "",
                ofSeconds(10),
                0,
                of(ofSeconds(10))
        );

    }
}
