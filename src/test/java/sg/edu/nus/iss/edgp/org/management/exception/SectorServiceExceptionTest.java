package sg.edu.nus.iss.edgp.org.management.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SectorServiceExceptionTest {

	@Test
	void testConstructorWithMessage() {
		String message = "Something went wrong";
		SectorServiceException exception = new SectorServiceException(message);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	void testConstructorWithMessageAndCause() {
		String message = "Service failure";
		Throwable cause = new RuntimeException("Database is down");

		SectorServiceException exception = new SectorServiceException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}
}
