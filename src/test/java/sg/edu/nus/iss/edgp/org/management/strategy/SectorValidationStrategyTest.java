package sg.edu.nus.iss.edgp.org.management.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.strategy.impl.SectorValidationStrategy;

@ExtendWith(MockitoExtension.class)
class SectorValidationStrategyTest {

	@Mock
	private SectorService sectorService;

	@InjectMocks
	private SectorValidationStrategy validationStrategy;

	private SectorRequest validRequest;

	@BeforeEach
	void setup() {
		validRequest = new SectorRequest();
		validRequest.setSectorName("Finance");
		validRequest.setSectorCode("FIN");
		validRequest.setSectorId("SEC001");
	}

	@Test
	void validateCreation_validInput_shouldReturnValidResult() {
		when(sectorService.findBySectorNameAndCode("Finance", "FIN")).thenReturn(Collections.emptyList());

		ValidationResult result = validationStrategy.validateCreation(validRequest);

		assertTrue(result.isValid());
		assertNull(result.getMessage());
	}

	@Test
	void validateCreation_missingName_shouldReturnInvalidResult() {
		validRequest.setSectorName("");

		ValidationResult result = validationStrategy.validateCreation(validRequest);

		assertFalse(result.isValid());
		assertEquals("Sector name is required", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}

	@Test
	void validateCreation_missingCode_shouldReturnInvalidResult() {
		validRequest.setSectorCode("");

		ValidationResult result = validationStrategy.validateCreation(validRequest);

		assertFalse(result.isValid());
		assertEquals("Sector code is required", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}

	@Test
	void validateCreation_duplicateSector_shouldReturnInvalidResult() {
		when(sectorService.findBySectorNameAndCode("Finance", "FIN")).thenReturn(List.of(new Sector()));

		ValidationResult result = validationStrategy.validateCreation(validRequest);

		assertFalse(result.isValid());
		assertEquals("Duplicate sector detected. Please enter a unique name.", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}

	@Test
	void validateUpdating_validSector_shouldReturnValidResult() {
		SectorDTO dto = new SectorDTO();
		dto.setSectorID("SEC001");

		when(sectorService.findBySectorId("SEC001")).thenReturn(dto);

		ValidationResult result = validationStrategy.validateUpdating(validRequest);

		assertTrue(result.isValid());
	}

	@Test
	void validateUpdating_missingSectorId_shouldReturnInvalidResult() {
		validRequest.setSectorId("");

		ValidationResult result = validationStrategy.validateUpdating(validRequest);

		assertFalse(result.isValid());
		assertEquals("Bad Request: Sector ID could not be blank.", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}

	@Test
	void validateUpdating_invalidSector_shouldReturnInvalidResult() {
		when(sectorService.findBySectorId("SEC001")).thenReturn(null);

		ValidationResult result = validationStrategy.validateUpdating(validRequest);

		assertFalse(result.isValid());
		assertEquals("Invalid sector ID.", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}
}
