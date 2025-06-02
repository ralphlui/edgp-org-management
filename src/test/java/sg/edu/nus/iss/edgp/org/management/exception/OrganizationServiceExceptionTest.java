package sg.edu.nus.iss.edgp.org.management.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationServiceExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Organization not found";
        OrganizationServiceException exception = new OrganizationServiceException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Error retrieving organization";
        Throwable cause = new RuntimeException("Database unavailable");

        OrganizationServiceException exception = new OrganizationServiceException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
