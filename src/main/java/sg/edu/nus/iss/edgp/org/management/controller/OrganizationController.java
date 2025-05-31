package sg.edu.nus.iss.edgp.org.management.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.APIResponse;
import sg.edu.nus.iss.edgp.org.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.dto.SearchRequest;
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
	@PreAuthorize("hasAuthority('SCOPE_org.manage')")
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
	
	@GetMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_org.manage')")
	public ResponseEntity<APIResponse<List<OrganizationDTO>>> retrieveActiveOrganizationList(
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @ModelAttribute SearchRequest searchRequest) {
		
		logger.info("Call active organization list API with page={}, size={}", searchRequest.getPage(), searchRequest.getSize());
		String message = "";
		String activityType = "Retrieve Active Organization List";
		String endpoint = "/api/orgs";
		String httpMethod = HttpMethod.GET.name();
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);
		
		try {
			Pageable pageable = PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize(),
					Sort.by("organizationName").ascending());
			Map<Long, List<OrganizationDTO>> resultMap = organizationService.retrieveActiveOrganizationList(pageable);
			logger.info("all active organization list size {}", resultMap.size());

			Map.Entry<Long, List<OrganizationDTO>> firstEntry = resultMap.entrySet().iterator().next();
			long totalRecord = firstEntry.getKey();
			List<OrganizationDTO> organizationDTOList = firstEntry.getValue();

			logger.info("totalRecord: {}", totalRecord);

			if (!organizationDTOList.isEmpty()) {
				message = "Successfully retrieved all active organization list.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(organizationDTOList, message, totalRecord));

			} else {
				message = "No Active Organization List.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.successWithEmptyData(organizationDTOList, message));
			}

			
		} catch (Exception e) {
			message = e instanceof OrganizationServiceException ? e.getMessage() : genericErrorMessage;
			logger.error(message);
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
	
	@GetMapping(value = "/my-organization", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_org.manage')")
	public ResponseEntity<APIResponse<OrganizationDTO>> getOrganizationbyOrgId(
			@RequestHeader("Authorization") String authorizationHeader, @RequestHeader("X-Org-Id") String orgId) {
		logger.info("Call orgainzation by org id API...");
		
		String message = "";
		String activityType = "Retrieve Organization by organization id";
		String endpoint = "/api/orgs/my-organization";
		String httpMethod = HttpMethod.GET.name();
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);
		
		try {
			
			if (orgId.isEmpty()) {
				message = "Bad Request: Organization id could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO, 400, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
			
			OrganizationDTO orgDTO = organizationService.findByOrganizationId(orgId);
			message = orgDTO.getOrganizationName() + " is found.";
			logger.info(message);
			auditService.logAudit(auditDTO, 200, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(orgDTO, message));
			
		} catch (Exception ex) {
			message = ex instanceof OrganizationServiceException ? ex.getMessage() : genericErrorMessage;
			logger.error(message);
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}	
	}
	
	
	@PutMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_org.manage')")
	public ResponseEntity<APIResponse<OrganizationDTO>> updateOrganization(
			@RequestHeader("Authorization") String authorizationHeader, @RequestHeader("X-Org-Id") String orgId,
			@RequestBody OrganizationRequest orgReq) {
		logger.info("Calling organization update API...");
		String message = "";
		String activityType = "Update Organization";
		String endpoint = "/api/orgs";
		String httpMethod = HttpMethod.PUT.name();
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);

		try {

			String jwtToken = authorizationHeader.substring(7);
			String userId = jwtService.extractSubject(jwtToken);
			orgReq.setOrganizationId(orgId);
			ValidationResult validationResult = organizationValidationStrategy.validateUpdating(orgReq);

			if (validationResult.isValid()) {
				OrganizationDTO orgDTO = organizationService.updateOrganization(orgReq, userId, orgId);
				message = orgDTO.getOrganizationName() + " is updated successfully.";
				logger.info(message);
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(orgDTO, message));
			}
			message = validationResult.getMessage();
			logger.error(message);
			auditService.logAudit(auditDTO, validationResult.getStatus().value(), message, authorizationHeader);
			return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));

		} catch (Exception ex) {
			message = ex instanceof OrganizationServiceException ? ex.getMessage() : genericErrorMessage;
			logger.error(message);
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));

		}
	}
	
	@GetMapping(value = "/users", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_org.manage')")
	public ResponseEntity<APIResponse<List<OrganizationDTO>>> getOrganizationListByUserId(
			@RequestHeader("Authorization") String authorizationHeader,
			 @Valid SearchRequest searchRequest) {
		
        logger.info("Call orgainzation list by user id API...");
		
		String message = "";
		String activityType = "Retrieve Organization by organization id";
		String endpoint = "/api/orgs/users";
		String httpMethod = HttpMethod.GET.name();
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);
		
		try {
			
			String jwtToken = authorizationHeader.substring(7);
			String userId = jwtService.extractSubject(jwtToken);
			
			
			Pageable pageable = PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize(),
					Sort.by("organizationName").ascending());
			Map<Long, List<OrganizationDTO>> resultMap = organizationService.findActiveOrganizationListByUserId(userId, pageable);
			logger.info("all active organization list size {}", resultMap.size());

			Map.Entry<Long, List<OrganizationDTO>> firstEntry = resultMap.entrySet().iterator().next();
			long totalRecord = firstEntry.getKey();
			List<OrganizationDTO> organizationDTOList = firstEntry.getValue();

			logger.info("totalRecord of getting organization list by user: {}", totalRecord);
			
			if (organizationDTOList.size() > 0) {
				message = "Successfully retrieved all active organization list by user.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(organizationDTOList, message, totalRecord));
			}  else {
				message = "No Active Organization List by this user.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.successWithEmptyData(organizationDTOList, message));
			}
			
		} catch (Exception ex) {
			message = ex instanceof OrganizationServiceException ? ex.getMessage() : genericErrorMessage;
			logger.error(message);
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
		
}
