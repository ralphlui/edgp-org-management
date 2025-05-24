package sg.edu.nus.iss.edgp.org.management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.APIResponse;
import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.strategy.impl.SectorValidationStrategy;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orgs/sectors")
public class SectorController {

	private static final Logger logger = LoggerFactory.getLogger(SectorController.class);

	private final SectorValidationStrategy sectorvalidationStrategy;
	private final SectorService sectorService;

	@PostMapping(value = "", produces = "application/json")
	@PreAuthorize("hasAuthority('SCOPE_manage')")
	public ResponseEntity<APIResponse<SectorDTO>> createSector(
			@RequestHeader("Authorization") String authorizationHeader, @RequestBody SectorRequest sectorRequest) {
		String message = "";
		try {
			ValidationResult validationResult = sectorvalidationStrategy.validateCreation(sectorRequest);

			if (validationResult.isValid()) {
				SectorDTO sectorDTO = sectorService.createSector(sectorRequest);
				message = "Success! The sector has been added.";
				logger.info(message);
				return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(sectorDTO, message));
			} else {
				message = validationResult.getMessage();
				logger.error(message);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));

			}

		} catch (Exception e) {
			message = "An error occurred while creating the sector.";
			logger.error(message);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}

	}

}