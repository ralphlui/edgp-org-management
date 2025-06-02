package sg.edu.nus.iss.edgp.org.management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
	private Sector sector;
	private Pageable pageable;
	private Sector dbSector;

	@BeforeEach
	void setup() {
		request = new SectorRequest();
		request.setSectorName("Finance");
		request.setSectorCode("FIN");
		request.setRemark("Important");

		savedSector = new Sector();
		savedSector.setSectorId("SEC001");

		expectedDto = new SectorDTO();
		expectedDto.setSectorName("Finance");

		sector = new Sector();
		sector.setSectorName("Finance");
		sector.setSectorCode("FIN");
		sector.setSectorId("SEC001");

		dbSector = new Sector();
		dbSector.setSectorId("SEC001");
		dbSector.setSectorName("Finance");

		pageable = PageRequest.of(0, 10);
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

	@Test
	void findBySectorNameAndCode_success() {
		List<Sector> mockResult = List.of(sector);

		Mockito.when(sectorRepository.findBySectorNameOrSectorCode("Finance", "FIN")).thenReturn(mockResult);

		List<Sector> result = sectorService.findBySectorNameAndCode("Finance", "FIN");

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Finance", result.get(0).getSectorName());
		verify(sectorRepository, times(1)).findBySectorNameOrSectorCode("Finance", "FIN");
	}

	@Test
	void findBySectorNameAndCode_shouldThrowException_whenRepositoryFails() {
		Mockito.when(sectorRepository.findBySectorNameOrSectorCode(anyString(), anyString()))
				.thenThrow(new RuntimeException("DB error"));

		SectorServiceException exception = assertThrows(SectorServiceException.class,
				() -> sectorService.findBySectorNameAndCode("Finance", "FIN"));

		assertEquals("An error occurred while searching for the sector by name and code", exception.getMessage());
		verify(sectorRepository, times(1)).findBySectorNameOrSectorCode("Finance", "FIN");
	}

	@Test
	void retrieveActiveSectorList_success() {
		Page<Sector> mockPage = new PageImpl<>(List.of(sector), pageable, 1);

		when(sectorRepository.findActiveSectorList(true, pageable)).thenReturn(mockPage);

		try (MockedStatic<DTOMapper> mocked = Mockito.mockStatic(DTOMapper.class)) {
			mocked.when(() -> DTOMapper.toSectorDTO(sector)).thenReturn(expectedDto);

			Map<Long, List<SectorDTO>> result = sectorService.retrieveActiveSectorList(pageable);

			assertNotNull(result);
			assertTrue(result.containsKey(1L));
			assertEquals(1, result.get(1L).size());
			assertEquals("Finance", result.get(1L).get(0).getSectorName());
		}

		verify(sectorRepository, times(1)).findActiveSectorList(true, pageable);
	}

	@Test
	void retrieveActiveSectorList_shouldThrowException_whenRepositoryFails() {
		when(sectorRepository.findActiveSectorList(true, pageable)).thenThrow(new RuntimeException("Database error"));

		SectorServiceException exception = assertThrows(SectorServiceException.class,
				() -> sectorService.retrieveActiveSectorList(pageable));

		assertEquals("An error occurred while retrieving active sector list", exception.getMessage());
		verify(sectorRepository, times(1)).findActiveSectorList(true, pageable);
	}

	@Test
	void updateSector_success() {

		Sector dbSector = new Sector();
		dbSector.setSectorId("SEC001");
		dbSector.setSectorName("Finance");

		Sector updatedSector = new Sector();
		updatedSector.setSectorId("SEC001");
		updatedSector.setSectorName("Finance");
		updatedSector.setDescription("Updated Description");

		request = new SectorRequest();
		request.setDescription("Updated Description");
		request.setRemark("Updated");
		request.setActive(true);

		expectedDto = new SectorDTO();
		expectedDto.setSectorID("SEC001");
		expectedDto.setSectorName("Finance");
		expectedDto.setDescription("Updated Description");

		when(sectorRepository.findBySectorId("SEC001")).thenReturn(Optional.of(dbSector));
		when(sectorRepository.save(any(Sector.class))).thenReturn(updatedSector);

		try (MockedStatic<DTOMapper> mocked = mockStatic(DTOMapper.class)) {
			mocked.when(() -> DTOMapper.toSectorDTO(updatedSector)).thenReturn(expectedDto);

			SectorDTO result = sectorService.updateSector(request, "user-001", "SEC001");

			assertNotNull(result);
			assertEquals("Finance", result.getSectorName());
			assertEquals("Updated Description", result.getDescription());

			verify(sectorRepository, times(1)).findBySectorId("SEC001");
			verify(sectorRepository, times(1)).save(any(Sector.class));
		}
	}

	@Test
	void updateSector_shouldThrow_whenSectorNotFound() {
		when(sectorRepository.findBySectorId("INVALID_ID")).thenReturn(null);

		assertThrows(SectorServiceException.class, () -> sectorService.updateSector(request, "user-001", "INVALID_ID"));

		verify(sectorRepository, times(1)).findBySectorId("INVALID_ID");
		verify(sectorRepository, never()).save(any(Sector.class));
	}

	@Test
	void findBySectorId_success() {
		when(sectorRepository.findBySectorId("SEC001")).thenReturn(Optional.of(sector));

		try (MockedStatic<DTOMapper> mocked = mockStatic(DTOMapper.class)) {
			mocked.when(() -> DTOMapper.toSectorDTO(sector)).thenReturn(expectedDto);

			SectorDTO result = sectorService.findBySectorId("SEC001");

			assertNotNull(result);
			assertEquals("Finance", result.getSectorName());

			verify(sectorRepository, times(1)).findBySectorId("SEC001");
		}
	}

	@Test
	void findBySectorId_repositoryThrowsException() {
		when(sectorRepository.findBySectorId("SEC999")).thenThrow(new RuntimeException("DB error"));

		SectorServiceException exception = assertThrows(SectorServiceException.class,
				() -> sectorService.findBySectorId("SEC999"));

		assertEquals("An error occurred while searching fot the sector by sector id", exception.getMessage());
		verify(sectorRepository, times(1)).findBySectorId("SEC999");
	}

	@Test
	void findBySectorIdAndIsActive_success() {
		Sector sector = new Sector();
		sector.setSectorId("SEC123");
		when(sectorRepository.findBySectorIdAndIsActive("SEC123", true)).thenReturn(sector);

		Sector result = sectorService.findBySectorIdAndIsActive("SEC123");

		assertNotNull(result);
		assertEquals("SEC123", result.getSectorId());
	}

	@Test
	void findBySectorIdAndIsActive_throwsException() {
		when(sectorRepository.findBySectorIdAndIsActive("SEC123", true)).thenThrow(new RuntimeException("DB error"));

		assertThrows(SectorServiceException.class, () -> sectorService.findBySectorIdAndIsActive("SEC123"));
	}

}
