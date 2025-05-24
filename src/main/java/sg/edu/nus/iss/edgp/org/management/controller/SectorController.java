package sg.edu.nus.iss.edgp.org.management.controller;

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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.APIResponse;
import sg.edu.nus.iss.edgp.org.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SearchRequest;
import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.org.management.exception.SectorServiceException;
import sg.edu.nus.iss.edgp.org.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.org.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.strategy.impl.SectorValidationStrategy;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orgs/sectors")
public class SectorController {

	private static final Logger logger = LoggerFactory.getLogger(SectorController.class);

	private final SectorValidationStrategy sectorvalidationStrategy;
	private final SectorService sectorService;
	private final JwtService jwtService;
	private final AuditService auditService;
	private String genericErrorMessage = "An error occurred while processing your request. Please try again later.";

	@PostMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_manage')")
	public ResponseEntity<APIResponse<SectorDTO>> createSector(
			@RequestHeader("Authorization") String authorizationHeader, @RequestBody SectorRequest sectorRequest) {
		
		logger.info("Calling create sector API ...");
		String message = "";
		String activityType = "Create Sector";
		String endpoint = "/api/orgs/sectors";
		String httpMethod = HttpMethod.POST.name();
		
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);

		try {
			String jwtToken = authorizationHeader.substring(7);
			String userId = jwtService.extractSubject(jwtToken);
			sectorRequest.setCreatedBy(userId);	
			ValidationResult validationResult = sectorvalidationStrategy.validateCreation(sectorRequest);

			if (validationResult.isValid()) {
				SectorDTO sectorDTO = sectorService.createSector(sectorRequest, userId);
				message = "Success! The sector has been added.";
				logger.info(message);
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(sectorDTO, message));
			} else {
				message = validationResult.getMessage();
				logger.error(message);
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));

			}

		} catch (Exception e) {
			message = e instanceof SectorServiceException ? e.getMessage() : genericErrorMessage;
			logger.error(message);
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}

	}
	
	@GetMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_manage')")
	public ResponseEntity<APIResponse<List<SectorDTO>>> retrieveActiveSectorList(
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @ModelAttribute SearchRequest searchRequest) {

		logger.info("Call sector getAll API with page={}, size={}", searchRequest.getPage(), searchRequest.getSize());
		String message = "";
		String activityType = "Retrieve Sector List";
		String endpoint = "/api/orgs/sectors";
		String httpMethod = HttpMethod.GET.name();
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);


		try {
			Pageable pageable = PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize(),
					Sort.by("sectorName").ascending());
			Map<Long, List<SectorDTO>> resultMap = sectorService.retrieveActiveSectorList(pageable);
			logger.info("all active sector list size {}", resultMap.size());

			Map.Entry<Long, List<SectorDTO>> firstEntry = resultMap.entrySet().iterator().next();
			long totalRecord = firstEntry.getKey();
			List<SectorDTO> sectorDTOList = firstEntry.getValue();

			logger.info("totalRecord: {}", totalRecord);

			if (!sectorDTOList.isEmpty()) {
				message = "Successfully retrieved all active sectors.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(sectorDTOList, message, totalRecord));

			} else {
				message = "No Active Sector List.";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.successWithEmptyData(sectorDTOList, message));
			}

		} catch (Exception ex) {
			message = ex instanceof SectorServiceException ? ex.getMessage() : genericErrorMessage;
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
	
	
	@PutMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_manage')")
	public ResponseEntity<APIResponse<SectorDTO>> updateSector(@RequestHeader("Authorization") String authorizationHeader,
			@RequestHeader("X-Sector-Id") String sectorId, @RequestBody SectorRequest sectorRequest) {
		logger.info("Calling sector update API...");
		String message = "";
		String activityType = "Update Sector";
		String endpoint = "/api/orgs/sectors";
		String httpMethod = HttpMethod.PUT.name();
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);
		
		try {
			String jwtToken = authorizationHeader.substring(7);
			String userId = jwtService.extractSubject(jwtToken);
			sectorRequest.setCreatedBy(userId);
			sectorRequest.setSectorId(sectorId);
			ValidationResult validationResult = sectorvalidationStrategy.validateUpdating(sectorRequest);
			if (validationResult.isValid()) {
				SectorDTO sectorDtO = sectorService.updateSector(sectorRequest, userId, sectorId);
				message = sectorDtO.getSectorName() + " is updated successfully.";
				logger.info(message);
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(sectorDtO, message));
			}
			message = validationResult.getMessage();
			logger.error(message);
			auditService.logAudit(auditDTO, 200, message, authorizationHeader);
			return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			
		} catch( Exception ex) {
			message = ex instanceof SectorServiceException ? ex.getMessage() : genericErrorMessage;
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
}