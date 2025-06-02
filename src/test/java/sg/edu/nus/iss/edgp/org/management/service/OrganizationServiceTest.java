package sg.edu.nus.iss.edgp.org.management.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.entity.Organization;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.exception.OrganizationServiceException;
import sg.edu.nus.iss.edgp.org.management.repository.OrganizationRepository;
import sg.edu.nus.iss.edgp.org.management.repository.SectorRepository;
import sg.edu.nus.iss.edgp.org.management.service.impl.OrganizationService;
import sg.edu.nus.iss.edgp.org.management.utility.DTOMapper;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

	@InjectMocks
	private OrganizationService organizationService;

	@Mock
	private SectorRepository sectorRepository;

	@Mock
	private OrganizationRepository organizationRepository;

	private final String ORG_NAME = "Test Org";
	private static final String VALID_UEN = "UEN123456";
	private Pageable pageable = PageRequest.of(0, 10);

	@Test
	void createOrganization_success() {
		// Prepare input
		OrganizationRequest request = new OrganizationRequest();
		request.setOrganizationName("Test Org");
		request.setAddress("Address 123");
		request.setContactNumber("12345678");
		request.setUniqueEntityNumber("UEN123");
		request.setStreetAddress("Street 1");
		request.setCity("City");
		request.setPostalCode("123456");
		request.setCountry("Country");
		request.setWebsiteURL("http://test.org");
		request.setOrganizationSize(30);

		SectorRequest sectorReq = new SectorRequest();
		sectorReq.setSectorId("SEC001");
		sectorReq.setSectorName("IT");
		Sector sector = new Sector();
		sector.setSectorId("SEC001");
		sector.setSectorName("IT");

		request.setSector(sector);
		request.setPrimaryContactName("Alice");
		request.setPrimaryContactEmail("alice@test.org");
		request.setPrimaryContactPosition("Manager");
		request.setPrimaryContactNumber("87654321");
		request.setRemark("Important Org");

		Organization savedOrg = new Organization();
		savedOrg.setOrganizationId("ORG123");
		savedOrg.setOrganizationName(request.getOrganizationName());
		savedOrg.setSector(sector);

		when(sectorRepository.findById("SEC001")).thenReturn(Optional.of(sector));
		when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);

		try (MockedStatic<DTOMapper> mapperMock = Mockito.mockStatic(DTOMapper.class)) {
			OrganizationDTO expectedDTO = new OrganizationDTO();
			expectedDTO.setOrganizationId("ORG123");
			expectedDTO.setOrganizationName("Test Org");

			mapperMock.when(() -> DTOMapper.toOrganizationDTO(savedOrg)).thenReturn(expectedDTO);

			OrganizationDTO result = organizationService.createOrganization(request, "user-001");

			assertNotNull(result);
			assertEquals("Test Org", result.getOrganizationName());
			verify(sectorRepository, times(1)).findById("SEC001");
			verify(organizationRepository, times(1)).save(any(Organization.class));
		}
	}

	@Test
	void createOrganization_sectorNotFound_throwsException() {

		OrganizationRequest request = new OrganizationRequest();
		Sector missingSector = new Sector();
		missingSector.setSectorId("UNKNOWN");
		missingSector.setSectorName("Missing Sector");
		request.setSector(missingSector);

		when(sectorRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

		OrganizationServiceException exception = assertThrows(OrganizationServiceException.class,
				() -> organizationService.createOrganization(request, "user-001"));

		assertTrue(exception.getMessage().contains("An error occured while creating organization"));

		verify(sectorRepository).findById("UNKNOWN");

		verify(organizationRepository, never()).save(any());
	}

	@Test
	void findByOrganizationName_success() {
		Organization mockOrg = new Organization();
		mockOrg.setOrganizationName(ORG_NAME);

		when(organizationRepository.findByOrganizationName(ORG_NAME)).thenReturn(mockOrg);

		Organization result = organizationService.findByOrganizationName(ORG_NAME);

		assertNotNull(result);
		assertEquals(ORG_NAME, result.getOrganizationName());
		verify(organizationRepository, times(1)).findByOrganizationName(ORG_NAME);
	}

	@Test
	void findByOrganizationName_repositoryThrowsException_shouldThrowServiceException() {
		when(organizationRepository.findByOrganizationName(ORG_NAME)).thenThrow(new RuntimeException("DB error"));

		OrganizationServiceException exception = assertThrows(OrganizationServiceException.class,
				() -> organizationService.findByOrganizationName(ORG_NAME));

		assertTrue(exception.getMessage().contains("An error occurred while searching for the organization by name"));
		verify(organizationRepository).findByOrganizationName(ORG_NAME);
	}

	@Test
	void findByUEN_success() {
		Organization mockOrganization = new Organization();
		mockOrganization.setUniqueEntityNumber(VALID_UEN);

		when(organizationRepository.findByUniqueEntityNumber(VALID_UEN)).thenReturn(mockOrganization);

		Organization result = organizationService.findByUEN(VALID_UEN);

		assertNotNull(result);
		assertEquals(VALID_UEN, result.getUniqueEntityNumber());
		verify(organizationRepository, times(1)).findByUniqueEntityNumber(VALID_UEN);
	}
	
	
	@Test
	void findByUEN_repositoryThrowsException_shouldThrowServiceException() {
		when(organizationRepository.findByUniqueEntityNumber(VALID_UEN))
				.thenThrow(new RuntimeException("DB access error"));

		OrganizationServiceException exception = assertThrows(OrganizationServiceException.class,
				() -> organizationService.findByUEN(VALID_UEN));

		assertTrue(exception.getMessage().contains("An error occurred while executing searching organization by UEN"));
		verify(organizationRepository).findByUniqueEntityNumber(VALID_UEN);
	}
	
	
	@Test
	void retrieveActiveOrganizationList_success() {
	    Organization mockOrg = new Organization();
	    mockOrg.setOrganizationName("TechOrg");

	    List<Organization> orgList = List.of(mockOrg);
	    Page<Organization> orgPage = new PageImpl<>(orgList, pageable, 1);

	    when(organizationRepository.findActiveOrganizationList(true, pageable))
	            .thenReturn(orgPage);

	    try (MockedStatic<DTOMapper> mocked = mockStatic(DTOMapper.class)) {
	        OrganizationDTO mockDTO = new OrganizationDTO();
	        mockDTO.setOrganizationName("TechOrg");

	        mocked.when(() -> DTOMapper.toOrganizationDTO(mockOrg)).thenReturn(mockDTO);

	        Map<Long, List<OrganizationDTO>> result = organizationService.retrieveActiveOrganizationList(pageable);

	        assertNotNull(result);
	        assertEquals(1, result.keySet().iterator().next());
	        assertEquals("TechOrg", result.values().iterator().next().get(0).getOrganizationName());
	        verify(organizationRepository, times(1)).findActiveOrganizationList(true, pageable);
	    }
	}
	
	@Test
	void retrieveActiveOrganizationList_repositoryThrowsException_shouldThrowServiceException() {
	    when(organizationRepository.findActiveOrganizationList(true, pageable))
	            .thenThrow(new RuntimeException("DB error"));

	    OrganizationServiceException ex = assertThrows(
	            OrganizationServiceException.class,
	            () -> organizationService.retrieveActiveOrganizationList(pageable)
	    );

	    assertTrue(ex.getMessage().contains("An error occurred while retrieving active organization list"));
	    verify(organizationRepository).findActiveOrganizationList(true, pageable);
	}

}