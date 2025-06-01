package sg.edu.nus.iss.edgp.org.management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

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

		sector = new Sector();
		sector.setSectorName("Finance");
		sector.setSectorCode("FIN");
		sector.setSectorId("SEC001");
		
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

        when(sectorRepository.findActiveSectorList(true, pageable))
                .thenReturn(mockPage);

        try (MockedStatic<DTOMapper> mocked = Mockito.mockStatic(DTOMapper.class)) {
            mocked.when(() -> DTOMapper.toSectorDTO(sector))
                  .thenReturn(expectedDto);

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
        when(sectorRepository.findActiveSectorList(true, pageable))
                .thenThrow(new RuntimeException("Database error"));

        SectorServiceException exception = assertThrows(
            SectorServiceException.class,
            () -> sectorService.retrieveActiveSectorList(pageable)
        );

        assertEquals("An error occurred while retrieving active sector list", exception.getMessage());
        verify(sectorRepository, times(1)).findActiveSectorList(true, pageable);
    }
}
