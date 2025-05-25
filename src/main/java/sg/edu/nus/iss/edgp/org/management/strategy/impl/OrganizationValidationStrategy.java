package sg.edu.nus.iss.edgp.org.management.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.org.management.service.impl.OrganizationService;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.strategy.IAPIHelperValidationStrategy;

@Service
@RequiredArgsConstructor
public class OrganizationValidationStrategy implements IAPIHelperValidationStrategy<OrganizationRequest> {

	private final OrganizationService organizationService;
	private final SectorService sectorService;

	@Override
	public ValidationResult validateCreation(OrganizationRequest orgReq) {
		ValidationResult validationResult = new ValidationResult();
		List<String> missingFields = new ArrayList<>();

		checkEmptyField(orgReq.getOrganizationName(), "Organization Name", missingFields);
		checkEmptyField(orgReq.getUniqueEntityNumber(), "Unique Entity Number", missingFields);
		checkEmptyField(orgReq.getPrimaryContactEmail(), "Primary Contact Email", missingFields);
		checkEmptyField(orgReq.getPrimaryContactName(), "Primary Contact Name", missingFields);
		checkEmptyField(orgReq.getPrimaryContactNumber(), "Primary Contact Number", missingFields);
		checkEmptyField(orgReq.getAddress(), "Address", missingFields);
		checkEmptyField(orgReq.getSector() != null ? orgReq.getSector().getSectorId() : null, "Organization Sector Id",
				missingFields);

		if (!missingFields.isEmpty()) {
			return buildValidationResult(String.join(" and ", missingFields) + " is required", HttpStatus.BAD_REQUEST);
		}

		if (organizationService.findByOrganizationName(orgReq.getOrganizationName().trim()) != null) {
			return buildValidationResult("Duplicate organization name detected. Please enter a unique name.",
					HttpStatus.BAD_REQUEST);
		}

		if (organizationService.findByUEN(orgReq.getUniqueEntityNumber()) != null) {
			return buildValidationResult("Duplicate organization UEN detected. Please enter a unique name.",
					HttpStatus.BAD_REQUEST);
		}
		
		if (sectorService.findBySectorId(orgReq.getSector().getSectorId()) == null) {
			return buildValidationResult("Sector not found with this sector name",
					HttpStatus.BAD_REQUEST);
		}

		validationResult.setValid(true);
		return validationResult;
	}

	private void checkEmptyField(String value, String fieldName, List<String> missingFields) {
		if (value == null || value.trim().isEmpty()) {
			missingFields.add(fieldName);
		}
	}

	private ValidationResult buildValidationResult(String message, HttpStatus status) {
		ValidationResult result = new ValidationResult();
		result.setMessage(message);
		result.setStatus(status);
		result.setValid(false);
		return result;
	}

	@Override
	public ValidationResult validateUpdating(OrganizationRequest data) {
		// TODO Auto-generated method stub
		return null;
	}

}
