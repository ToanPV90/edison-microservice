package de.otto.edison.validation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.validation.configuration.AggregateResourceBundleMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ErrorHalRepresentationFactoryTest {


    private AggregateResourceBundleMessageSource messageSource;

    @BeforeEach
    public void setUp() {
        messageSource = new AggregateResourceBundleMessageSource();
        messageSource.setBasename("ValidationMessages");
        messageSource.setUseCodeAsDefaultMessage(true);

    }

    @Test
    public void shouldBuildRepresentationForErrorMessage() {
        // given
        String someErrorProfile = "someErrorProfile";
        String someErrorMessage = "someErrorMessage";
        final ErrorHalRepresentationFactory factory = new ErrorHalRepresentationFactory(messageSource, new ObjectMapper(), someErrorProfile);

        // when
        ErrorHalRepresentation errorHalRepresentation = factory.halRepresentationForErrorMessage(someErrorMessage);

        // then
        assertThat(errorHalRepresentation.getErrors().isEmpty(), is(true));
        assertThat(errorHalRepresentation.getErrorMessage(), is(someErrorMessage));
        assertThat(errorHalRepresentation.getLinks().getLinkBy("profile").isPresent(), is(true));
        assertThat(errorHalRepresentation.getLinks().getLinkBy("profile").get().getHref(), is(someErrorProfile));
    }

    @Test
    public void shouldBuildRepresentationForValidationResults() {
        // given
        final ErrorHalRepresentationFactory factory = new ErrorHalRepresentationFactory(messageSource, new ObjectMapper(), "someErrorProfile");

        // when
        final Errors mockErrors = mock(Errors.class);
        final FieldError fieldError = new FieldError("someObject",
                "xyzField",
                "rejected",
                true,
                new String[]{"NotEmpty"},
                new Object[]{},
                "Some default message");
        when(mockErrors.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        when(mockErrors.getErrorCount()).thenReturn(1);
        final ErrorHalRepresentation errorHalRepresentation = factory.halRepresentationForValidationErrors(mockErrors);

        // then
        assertThat(errorHalRepresentation.getErrorMessage(), is("Validation failed. 1 error(s)"));
        assertThat(errorHalRepresentation.getLinks().getLinkBy("profile").isPresent(), is(true));
        assertThat(errorHalRepresentation.getLinks().getLinkBy("profile").get().getHref(), is("someErrorProfile"));
        final List<Map<String, String>> listOfViolations = errorHalRepresentation.getErrors().get("xyzField");
        assertThat(listOfViolations, hasSize(1));
        assertThat(listOfViolations.get(0), hasEntry("key", "text.not_empty"));
        assertThat(listOfViolations.get(0), hasEntry("message", "Some default message"));
        assertThat(listOfViolations.get(0), hasEntry("rejected", "rejected"));
    }

    @Test
    public void shouldNotCrashOnNullValues() {
        // given
        final ErrorHalRepresentationFactory factory = new ErrorHalRepresentationFactory(messageSource, new ObjectMapper(), "someErrorProfile");

        // when
        final Errors mockErrors = mock(Errors.class);
        final FieldError fieldError = new FieldError("someObject",
                "xyzField",
                null,
                true,
                new String[]{"NotEmpty"},
                new Object[]{},
                "Some default message");
        when(mockErrors.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        when(mockErrors.getErrorCount()).thenReturn(1);
        final ErrorHalRepresentation errorHalRepresentation = factory.halRepresentationForValidationErrors(mockErrors);

        // then
        assertThat(errorHalRepresentation.getErrorMessage(), is("Validation failed. 1 error(s)"));
        assertThat(errorHalRepresentation.getLinks().getLinkBy("profile").isPresent(), is(true));
        assertThat(errorHalRepresentation.getLinks().getLinkBy("profile").get().getHref(), is("someErrorProfile"));
        final List<Map<String, String>> listOfViolations = errorHalRepresentation.getErrors().get("xyzField");
        assertThat(listOfViolations, hasSize(1));
        assertThat(listOfViolations.get(0), hasEntry("key", "text.not_empty"));
        assertThat(listOfViolations.get(0), hasEntry("message", "Some default message"));
        assertThat(listOfViolations.get(0), hasEntry("rejected", "null"));
    }
}