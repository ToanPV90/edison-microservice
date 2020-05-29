package de.otto.edison.jobs.service;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.testsupport.util.Sets.hashSet;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JobMutexGroupsTest {

    @Configuration
    static class TestMutexGroupConfiguration {
        @Bean
        JobMutexGroup first() {
            return new JobMutexGroup("first", "foo", "foo1", "foo2");
        }
        @Bean
        JobMutexGroup second() {
            return new JobMutexGroup("second", "bar", "bar1", "foo1");
        }
    }

    @Test
    public void shouldAutowireMultipleMutexGroups() {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestMutexGroupConfiguration.class);
            context.register(JobMutexGroups.class);
            context.refresh();

            final JobMutexGroups jobMutexGroups = context.getBean("jobMutexGroups", JobMutexGroups.class);
            assertThat(jobMutexGroups.getMutexGroups(), hasSize(2));
        }
    }

    @Test
    public void shouldNeverMutuallyExcludeSameType() {
        final JobMutexGroups mutexGroups = new JobMutexGroups();
        mutexGroups.setMutexGroups(singleton(
                new JobMutexGroup("test", "foo", "bar"))
        );

        assertThat(mutexGroups.mutexJobTypesFor("foo"), contains("bar"));
    }

    @Test
    public void shouldMutuallyExcludeOthers() {
        final JobMutexGroups mutexGroups = new JobMutexGroups();
        mutexGroups.setMutexGroups(hashSet(
                new JobMutexGroup("first", "foo", "foo1", "foo2"),
                new JobMutexGroup("second", "bar", "bar1", "foo1")
        ));
        assertThat(mutexGroups.mutexJobTypesFor("foo1"), containsInAnyOrder("foo", "foo2", "bar", "bar1"));;
    }
}
