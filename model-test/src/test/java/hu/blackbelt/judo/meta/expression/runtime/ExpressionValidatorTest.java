package hu.blackbelt.judo.meta.expression.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpressionValidatorTest {

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Tests for format method
    @Test
    void testFormat_noErrorsNoWarnings() {
        String result = ExpressionValidator.format(Collections.emptyList(), Collections.emptyList());
        assertEquals("All constraints have been satisfied", result);
    }

    @Test
    void testFormat_withErrorsNoWarnings() {
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        String result = ExpressionValidator.format(errors, Collections.emptyList());
        assertTrue(result.contains("\t2 error(s)"));
        assertTrue(result.contains("\tError 1"));
        assertTrue(result.contains("\tError 2"));
        assertFalse(result.contains("warning"));
    }

    @Test
    void testFormat_noErrorsWithWarnings() {
        List<String> warnings = Arrays.asList("Warning 1", "Warning 2");
        String result = ExpressionValidator.format(Collections.emptyList(), warnings);
        assertTrue(result.contains("\t2 warning(s)"));
        assertTrue(result.contains("\tWarning 1"));
        assertTrue(result.contains("\tWarning 2"));
        assertFalse(result.contains("error"));
    }

    @Test
    void testFormat_withErrorsAndWarnings() {
        List<String> errors = Arrays.asList("Error 1");
        List<String> warnings = Arrays.asList("Warning 1");
        String result = ExpressionValidator.format(errors, warnings);
        assertTrue(result.contains("\t1 error(s)"));
        assertTrue(result.contains("\tError 1"));
        assertTrue(result.contains("\t1 warning(s)"));
        assertTrue(result.contains("\tWarning 1"));
        assertTrue(result.contains("\t\n")); // Check for the joiner
    }

    // Tests for validate method
    @Test
    void testValidate_noExpectationsNoUnsatisfied() throws ExpressionValidationException {
        ExpressionValidator.validate(mockLogger, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        verifyNoInteractions(mockLogger);
    }

    @Test
    void testValidate_noExpectationsWithUnsatisfiedErrors() {
        List<String> unsatisfiedErrors = Arrays.asList("Unsatisfied Error");
        assertThrows(ExpressionValidationException.class, () ->
                ExpressionValidator.validate(mockLogger, Collections.emptyList(), Collections.emptyList(), unsatisfiedErrors, Collections.emptyList()));
    }

    @Test
    void testValidate_noExpectationsWithUnsatisfiedWarnings() throws ExpressionValidationException {
        List<String> unsatisfiedWarnings = Arrays.asList("Unsatisfied Warning");
        ExpressionValidator.validate(mockLogger, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), unsatisfiedWarnings);
        verify(mockLogger).warn(anyString());
    }

    @Test
    void testValidate_expectedErrorsMatch() throws ExpressionValidationException {
        List<String> expectedErrors = Arrays.asList("Error A", "Error B");
        List<String> unsatisfiedErrors = Arrays.asList("Error A", "Error B");
        ExpressionValidator.validate(mockLogger, expectedErrors, Collections.emptyList(), unsatisfiedErrors, Collections.emptyList());
    }

    @Test
    void testValidate_expectedWarningsMatch() throws ExpressionValidationException {
        List<String> expectedWarnings = Arrays.asList("Warning A", "Warning B");
        List<String> unsatisfiedWarnings = Arrays.asList("Warning A", "Warning B");
        ExpressionValidator.validate(mockLogger, Collections.emptyList(), expectedWarnings, Collections.emptyList(), unsatisfiedWarnings);
    }

    @Test
    void testValidate_unexpectedErrors() {
        List<String> expectedErrors = Arrays.asList("Error A");
        List<String> unsatisfiedErrors = Arrays.asList("Error A", "Error B"); // Error B is unexpected
        assertThrows(ExpressionValidationException.class, () ->
                ExpressionValidator.validate(mockLogger, expectedErrors, Collections.emptyList(), unsatisfiedErrors, Collections.emptyList()));
    }

    @Test
    void testValidate_errorsNotFound() {
        List<String> expectedErrors = Arrays.asList("Error A", "Error B"); // Error B not found
        List<String> unsatisfiedErrors = Arrays.asList("Error A");
        assertThrows(ExpressionValidationException.class, () ->
                ExpressionValidator.validate(mockLogger, expectedErrors, Collections.emptyList(), unsatisfiedErrors, Collections.emptyList()));
    }

    @Test
    void testValidate_unexpectedWarnings() throws ExpressionValidationException {
        List<String> expectedWarnings = Arrays.asList("Warning A");
        List<String> unsatisfiedWarnings = Arrays.asList("Warning A", "Warning B"); // Warning B is unexpected
        ExpressionValidator.validate(mockLogger, Collections.emptyList(), expectedWarnings, Collections.emptyList(), unsatisfiedWarnings);
        verify(mockLogger).warn(anyString());
    }

    @Test
    void testValidate_warningsNotFound() {
        List<String> expectedWarnings = Arrays.asList("Warning A", "Warning B"); // Warning B not found
        List<String> unsatisfiedWarnings = Arrays.asList("Warning A");
        assertThrows(ExpressionValidationException.class, () ->
                ExpressionValidator.validate(mockLogger, Collections.emptyList(), expectedWarnings, Collections.emptyList(), unsatisfiedWarnings));
    }

    @Test
    void testValidate_allMismatches() {
        List<String> expectedErrors = Arrays.asList("E1");
        List<String> expectedWarnings = Arrays.asList("W1");
        List<String> unsatisfiedErrors = Arrays.asList("E2");
        List<String> unsatisfiedWarnings = Arrays.asList("W2");
        assertThrows(ExpressionValidationException.class, () ->
                ExpressionValidator.validate(mockLogger, expectedErrors, expectedWarnings, unsatisfiedErrors, unsatisfiedWarnings));
    }

}