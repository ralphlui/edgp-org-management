package sg.edu.nus.iss.edgp.org.management.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.strategy.IAPIHelperValidationStrategy;
import sg.edu.nus.iss.edgp.org.management.utility.GeneralUtility;

@Service
@RequiredArgsConstructor
public class SectorValidationStrategy implements IAPIHelperValidationStrategy<SectorRequest> {

	private final SectorService sectorService;

	@Override
	public ValidationResult validateCreation(SectorRequest sectorReq) {
		ValidationResult validationResult = new ValidationResult();

		List<String> missingFields = new ArrayList<>();
		if (sectorReq.getSectorName().isEmpty())
			missingFields.add("Sector name");
		if (sectorReq.getSectorCode().isEmpty())
			missingFields.add("Sector code");
		if (sectorReq.getCreatedBy().isEmpty())
			missingFields.add("Created User ID");

		if (!missingFields.isEmpty()) {
			validationResult.setMessage(String.join(" and ", missingFields) + " is required");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		List<Sector> dbSectorList = sectorService.findBySectorNameAndCode(sectorReq.getSectorName().trim(),
				sectorReq.getSectorCode().trim());
		if (!dbSectorList.isEmpty()) {
			validationResult.setMessage("Duplicate sector detected. Please enter a unique name.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		validationResult.setValid(true);

		return validationResult;
	}

	@Override
	public ValidationResult validateUpdating(String userId, String sectorId) {
		ValidationResult validationResult = new ValidationResult();

		sectorId = GeneralUtility.makeNotNull(sectorId);
		userId = GeneralUtility.makeNotNull(userId);

		if (sectorId.isEmpty()) {
			return buildInvalidResult("Bad Request: Sector ID could not be blank.");
		}

		Sector sector = sectorService.findBySectorId(sectorId);
		if (sector == null || sector.getSectorId().isEmpty()) {
			return buildInvalidResult("Invalid sector ID: " + sectorId);
		}

		if (userId.isEmpty()) {
			return buildInvalidResult("Bad Request: User ID field could not be blank.");
		}

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
