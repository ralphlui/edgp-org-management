package sg.edu.nus.iss.edgp.org.management.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.dto.ValidationResult;
import sg.edu.nus.iss.edgp.org.management.entity.Organization;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.org.management.service.impl.OrganizationService;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.strategy.impl.OrganizationValidationStrategy;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;

@ExtendWith(MockitoExtension.class)
class OrganizationValidationStrategyTest {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private SectorService sectorService;
    
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private OrganizationValidationStrategy validationStrategy;

    private OrganizationRequest validRequest;
    private final String validOrgId = "ORG123";
    private final String jwtToken = "test.jwt.token";
    private final String authorizationHeader = "Bearer " + jwtToken;

    @BeforeEach
    void setup() {
        validRequest = new OrganizationRequest();
        validRequest.setOrganizationId("ORG001");
        validRequest.setOrganizationName("Acme Org");
        validRequest.setUniqueEntityNumber("UEN123456");
        validRequest.setPrimaryContactEmail("contact@acme.org");
        validRequest.setPrimaryContactName("John Doe");
        validRequest.setPrimaryContactNumber("12345678");
        validRequest.setAddress("123 Street");

        Sector sector = new Sector();
        sector.setSectorId("SEC123");
        validRequest.setSector(sector);
    }

    @Test
    void validateCreation_success() {
        when(organizationService.findByOrganizationName("Acme Org")).thenReturn(null);
        when(organizationService.findByUEN("UEN123456")).thenReturn(null);
        when(sectorService.findBySectorIdAndIsActive("SEC123")).thenReturn(new Sector());

        ValidationResult result = validationStrategy.validateCreation(validRequest);

        assertTrue(result.isValid());
    }

    @Test
    void validateCreation_missingFields_shouldReturnInvalid() {
        OrganizationRequest req = new OrganizationRequest(); // empty object

        ValidationResult result = validationStrategy.validateCreation(req);

        assertFalse(result.isValid());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertTrue(result.getMessage().contains("Organization Name"));
    }

    @Test
    void validateCreation_duplicateName_shouldReturnInvalid() {
        when(organizationService.findByOrganizationName("Acme Org")).thenReturn(new Organization());

        ValidationResult result = validationStrategy.validateCreation(validRequest);

        assertFalse(result.isValid());
        assertEquals("Duplicate organization name detected. Please enter a unique name.", result.getMessage());
    }

    @Test
    void validateCreation_duplicateUEN_shouldReturnInvalid() {
        when(organizationService.findByOrganizationName("Acme Org")).thenReturn(null);
        when(organizationService.findByUEN("UEN123456")).thenReturn(new Organization());

        ValidationResult result = validationStrategy.validateCreation(validRequest);

        assertFalse(result.isValid());
        assertEquals("Duplicate organization UEN detected. Please enter a unique UEN.", result.getMessage());
    }

    @Test
    void validateCreation_invalidSector_shouldReturnInvalid() {
        when(organizationService.findByOrganizationName("Acme Org")).thenReturn(null);
        when(organizationService.findByUEN("UEN123456")).thenReturn(null);
        when(sectorService.findBySectorIdAndIsActive("SEC123")).thenReturn(null);

        ValidationResult result = validationStrategy.validateCreation(validRequest);

        assertFalse(result.isValid());
        assertEquals("Active Sector not found with this sector name", result.getMessage());
    }

    @Test
    void validateUpdating_success() {
        when(organizationService.findByOrganizationId("ORG001")).thenReturn(new OrganizationDTO() {{
            setOrganizationId("ORG001");
        }});
        when(sectorService.findBySectorIdAndIsActive("SEC123")).thenReturn(new Sector());

        ValidationResult result = validationStrategy.validateUpdating(validRequest);

        assertTrue(result.isValid());
    }

    @Test
    void validateUpdating_missingOrgId_shouldReturnInvalid() {
        validRequest.setOrganizationId("");

        ValidationResult result = validationStrategy.validateUpdating(validRequest);

        assertFalse(result.isValid());
        assertEquals("Bad Request: Organization ID could not be blank.", result.getMessage());
    }

    @Test
    void validateUpdating_invalidOrg_shouldReturnInvalid() {
        when(organizationService.findByOrganizationId("ORG001")).thenReturn(null);

        ValidationResult result = validationStrategy.validateUpdating(validRequest);

        assertFalse(result.isValid());
        assertEquals("Invalid organization ID.", result.getMessage());
    }

    @Test
    void validateUpdating_invalidSector_shouldReturnInvalid() {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setOrganizationId("ORG001");
        when(organizationService.findByOrganizationId("ORG001")).thenReturn(dto);
        when(sectorService.findBySectorIdAndIsActive("SEC123")).thenReturn(null);

        ValidationResult result = validationStrategy.validateUpdating(validRequest);

        assertFalse(result.isValid());
        assertEquals("Active Sector not found with this sector name", result.getMessage());
    }
    
    
    @Test
    void validateObject_shouldReturnErrorWhenOrgIdIsBlank() {
        ValidationResult result = validationStrategy.validateObject("  ", authorizationHeader);
        assertFalse(result.isValid());
        assertEquals("Bad Request: Organization id cannot be blank.", result.getMessage());
    }

    @Test
    void validateObject_shouldReturnErrorWhenAuthorizationHeaderIsInvalid() {
        ValidationResult result = validationStrategy.validateObject(validOrgId, "InvalidHeader");
        assertFalse(result.isValid());
        assertEquals("Invalid Authorization header.", result.getMessage());
    }

    @Test
    void validateObject_shouldReturnErrorWhenScopeIsViewAndOrgIdDoesNotMatch() {
        when(jwtService.extractScopeFromToken(jwtToken)).thenReturn("view");
        when(jwtService.extractOrgIdFromToken(jwtToken)).thenReturn("OTHER_ORG");

        ValidationResult result = validationStrategy.validateObject(validOrgId, authorizationHeader);

        assertFalse(result.isValid());
        assertEquals("Access Denied. Not authorized to view this organization.", result.getMessage());
    }

    @Test
    void validateObject_shouldReturnValidWhenScopeIsViewAndOrgIdMatches() {
        when(jwtService.extractScopeFromToken(jwtToken)).thenReturn("view");
        when(jwtService.extractOrgIdFromToken(jwtToken)).thenReturn(validOrgId);

        ValidationResult result = validationStrategy.validateObject(validOrgId, authorizationHeader);

        assertTrue(result.isValid());
    }

    @Test
    void validateObject_shouldReturnValidWhenScopeIsManage() {
        when(jwtService.extractScopeFromToken(jwtToken)).thenReturn("manage");

        ValidationResult result = validationStrategy.validateObject(validOrgId, authorizationHeader);

        assertTrue(result.isValid());
    }
}

