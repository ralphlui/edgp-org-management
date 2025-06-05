package sg.edu.nus.iss.edgp.org.management.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.org.management.service.impl.OrganizationService;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.strategy.IAPIHelperValidationStrategy;
import sg.edu.nus.iss.edgp.org.management.utility.GeneralUtility;

@Service
@RequiredArgsConstructor
public class OrganizationValidationStrategy implements IAPIHelperValidationStrategy<OrganizationRequest> {

	private final OrganizationService organizationService;
	private final SectorService sectorService;
	private final JwtService jwtService;

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
			return buildValidationResult("Duplicate organization UEN detected. Please enter a unique UEN.",
					HttpStatus.BAD_REQUEST);
		}
		
		if (sectorService.findBySectorIdAndIsActive(orgReq.getSector().getSectorId()) == null) {
			return buildValidationResult("Active Sector not found with this sector name",
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
	public ValidationResult validateUpdating(OrganizationRequest orgReq) {
		ValidationResult validationResult = new ValidationResult();

		String orgId = GeneralUtility.makeNotNull(orgReq.getOrganizationId());

		if (orgId.isEmpty()) {
			return buildInvalidResult("Bad Request: Organization ID could not be blank.");
		}

		OrganizationDTO organization = organizationService.findByOrganizationId(orgId);
		if (organization == null || organization.getOrganizationId().isEmpty()) {
			return buildInvalidResult("Invalid organization ID.");
		}
		
		Sector sector = orgReq.getSector();
		if ( sector != null && sector.getSectorId()!= null && !sector.getSectorId().isEmpty() && sectorService.findBySectorIdAndIsActive(orgReq.getSector().getSectorId()) == null) {
			return buildValidationResult("Active Sector not found with this sector name",
					HttpStatus.BAD_REQUEST);
		}

		validationResult.setValid(true);
		return validationResult;
	}
	
	public ValidationResult validateObject(String orgId, String authorizationHeader) {
	    if (orgId == null || orgId.isBlank()) {
	        return buildInvalidResult("Bad Request: Organization id cannot be blank.");
	    }

	    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
	        return buildInvalidResult("Invalid Authorization header.");
	    }

	    String jwtToken = authorizationHeader.substring(7);
	    String scope = jwtService.extractScopeFromToken(jwtToken);

	    if (scope.isEmpty() || scope.toLowerCase().contains("view") || scope.toLowerCase().contains("invalid")) {
	        String userOrgId = jwtService.extractOrgIdFromToken(jwtToken);
	        if (!orgId.equals(userOrgId)) {
	            return buildInvalidResult("Access Denied. Not authorized to view this organization.");
	        }
	    }

	    ValidationResult validationResult = new ValidationResult();
	    validationResult.setValid(true);
	    return validationResult;
	}


	private ValidationResult buildInvalidResult(String message) {
		ValidationResult result = new ValidationResult();
		result.setMessage(message);
		result.setValid(false);
		result.setStatus(HttpStatus.BAD_REQUEST);
		return result;
	}
	
	


}
