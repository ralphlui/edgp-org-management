package sg.edu.nus.iss.edgp.org.management.strategy;

import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;

public interface IAPIHelperValidationStrategy<T> {
	ValidationResult validateCreation(T data);
	
	ValidationResult validateUpdating(String firstData, String secondData);

}
