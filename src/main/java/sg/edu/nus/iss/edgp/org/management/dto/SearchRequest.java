package sg.edu.nus.iss.edgp.org.management.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequest {

	@Min(1)
	private int page = 1;

	@Min(1)
	private int size = 50;
	
	private boolean noPagination = false; 

}
