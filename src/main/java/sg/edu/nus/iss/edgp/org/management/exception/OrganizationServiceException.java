package sg.edu.nus.iss.edgp.org.management.exception;

public class OrganizationServiceException extends RuntimeException  {

	private static final long serialVersionUID = 1L;

	public OrganizationServiceException(String message) {
		super(message);
	}
	
	public OrganizationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
