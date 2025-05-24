package sg.edu.nus.iss.edgp.org.management.exception;

public class SectorNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SectorNotFoundException(String message) {
		super(message);
	}
	
	public SectorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
