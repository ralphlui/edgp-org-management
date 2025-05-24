package sg.edu.nus.iss.edgp.org.management.exception;

public class SectorServiceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SectorServiceException(String message) {
		super(message);
	}
	
	public SectorServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
