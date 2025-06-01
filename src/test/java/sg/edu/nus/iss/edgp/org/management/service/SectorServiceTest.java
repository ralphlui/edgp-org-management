package sg.edu.nus.iss.edgp.org.management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.exception.SectorServiceException;
import sg.edu.nus.iss.edgp.org.management.repository.SectorRepository;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.utility.DTOMapper;

@ExtendWith(MockitoExtension.class)
class SectorServiceTest {

	@Mock
	private SectorRepository sectorRepository;
	@InjectMocks
	private SectorService sectorService;

	private SectorRequest request;
	private Sector savedSector;
	private SectorDTO expectedDto;

	@BeforeEach
	void setup() {
		request = new SectorRequest();
		request.setSectorName("Finance");
		request.setSectorCode("FIN");
		request.setRemark("Important");

		savedSector = new Sector();
		savedSector.setSectorId("S-001");

		expectedDto = new SectorDTO();
		expectedDto.setSectorName("Finance");
	}

	@Test
	void createSector_success() {
		when(sectorRepository.save(any(Sector.class))).thenReturn(savedSector);

		try (MockedStatic<DTOMapper> mocked = mockStatic(DTOMapper.class)) {
			mocked.when(() -> DTOMapper.toSectorDTO(any(Sector.class))).thenReturn(expectedDto);

			SectorDTO result = sectorService.createSector(request, "user-001");

			assertNotNull(result);
			assertEquals("Finance", result.getSectorName());
		}
	}

	@Test
	void createSector_repositoryThrowsException_shouldThrowServiceException() {
		when(sectorRepository.save(any(Sector.class))).thenThrow(new RuntimeException("DB is down"));

		SectorServiceException ex = assertThrows(SectorServiceException.class,
				() -> sectorService.createSector(request, "user-001"));

		assertTrue(ex.getMessage().contains("An error occured while creating sector"));
	}
}
