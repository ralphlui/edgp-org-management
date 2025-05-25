package sg.edu.nus.iss.edgp.org.management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.APIResponse;
import sg.edu.nus.iss.edgp.org.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.org.management.exception.OrganizationServiceException;
import sg.edu.nus.iss.edgp.org.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.org.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.org.management.service.impl.OrganizationService;
import sg.edu.nus.iss.edgp.org.management.strategy.impl.OrganizationValidationStrategy;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orgs")
public class OrganizationController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);
	private final AuditService auditService;
	private final JwtService jwtService;
	private final OrganizationValidationStrategy organizationValidationStrategy;
	private final OrganizationService organizationService;
	
	private String genericErrorMessage = "An error occurred while processing your request. Please try again later.";


	@PostMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_manage')")
	public ResponseEntity<APIResponse<OrganizationDTO>> createOrganization(
			@RequestHeader("Authorization") String authorizationHeader, @RequestBody OrganizationRequest orgRequest) {
		
		logger.info("Calling create Organization API ...");
		String message = "";
		String activityType = "Create Organization";
		String endpoint = "/api/orgs";
		String httpMethod = HttpMethod.POST.name();
		
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);
		
		try {
			
			String jwtToken = authorizationHeader.substring(7);
			String userId = jwtService.extractSubject(jwtToken);
			ValidationResult validationResult = organizationValidationStrategy.validateCreation(orgRequest);
			if (validationResult.isValid()) {
				OrganizationDTO organizationDTO = organizationService.createOrganization(orgRequest, userId);
				message = "Success! The organization has been added.";
				logger.info(message);
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(organizationDTO, message));
			} else {
				message = validationResult.getMessage();
				logger.error(message);
				auditService.logAudit(auditDTO, validationResult.getStatus().value(), message, authorizationHeader);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			}	
		} catch (Exception e) {
			message = e instanceof OrganizationServiceException ? e.getMessage() : genericErrorMessage;
			logger.error(message);
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}

	}
}
