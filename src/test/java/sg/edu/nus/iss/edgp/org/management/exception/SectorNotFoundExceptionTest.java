package sg.edu.nus.iss.edgp.org.management.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SectorNotFoundExceptionTest {

	@Test
	void testConstructorWithMessage() {
		String message = "Sector not found";
		SectorNotFoundException exception = new SectorNotFoundException(message);

		assertEquals(message, exception.getMessage());
	}

	@Test
	void testConstructorWithMessageAndCause() {
		String message = "Sector not found";
		Throwable cause = new RuntimeException("Database error");
		SectorNotFoundException exception = new SectorNotFoundException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}
}
