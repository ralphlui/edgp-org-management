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

@Service
@RequiredArgsConstructor
public class SectorValidationStrategy implements IAPIHelperValidationStrategy<SectorRequest> {

	private final SectorService sectorService;
	
	@Override
	public ValidationResult validateCreation(SectorRequest sectorReq) {
		ValidationResult validationResult = new ValidationResult();
		
		List<String> missingFields = new ArrayList<>();
		if (sectorReq.getSectorName().isEmpty()) missingFields.add("Sector name");
		if (sectorReq.getSectorCode().isEmpty()) missingFields.add("Sector code");
		if (sectorReq.getCreatedBy().isEmpty()) missingFields.add("Created User ID");

		if (!missingFields.isEmpty()) {
		    validationResult.setMessage(String.join(" and ", missingFields) + " is required");
		    validationResult.setStatus(HttpStatus.BAD_REQUEST);
		    validationResult.setValid(false);
		    return validationResult;
		}
		
		List<Sector> dbSectorList = sectorService.findBySectorNameAndCode(sectorReq.getSectorName().trim(), sectorReq.getSectorCode().trim());
		if (!dbSectorList.isEmpty()) {
			validationResult.setMessage("Duplicate sector detected. Please enter a unique name.");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}
		
		validationResult.setValid(true);

		return validationResult;
	}

	
}

