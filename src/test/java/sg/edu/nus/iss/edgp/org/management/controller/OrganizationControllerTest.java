package sg.edu.nus.iss.edgp.org.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sg.edu.nus.iss.edgp.org.management.dto.*;
import sg.edu.nus.iss.edgp.org.management.exception.OrganizationServiceException;
import sg.edu.nus.iss.edgp.org.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.org.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.org.management.service.impl.OrganizationService;
import sg.edu.nus.iss.edgp.org.management.strategy.impl.OrganizationValidationStrategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebMvcTest(OrganizationController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrganizationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrganizationService organizationService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private AuditService auditService;

	@MockitoBean
	private OrganizationValidationStrategy organizationValidationStrategy;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createOrganization_success() throws Exception {
		OrganizationRequest request = new OrganizationRequest();
		request.setOrganizationName("Test Org");

		OrganizationDTO dto = new OrganizationDTO();
		dto.setOrganizationName("Test Org");

		AuditDTO audit = new AuditDTO();
		ValidationResult validResult = new ValidationResult();
		validResult.setValid(true);

		when(jwtService.extractSubject("valid-token")).thenReturn("user-123");
		when(organizationValidationStrategy.validateCreation(any())).thenReturn(validResult);
		when(organizationService.createOrganization(any(), any())).thenReturn(dto);
		when(auditService.createAuditDTO(any(), any(), any())).thenReturn(audit);

		mockMvc.perform(post("/api/orgs").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.organizationName").value("Test Org"));
	}

	@Test
	void createOrganization_validationFailure() throws Exception {
		OrganizationRequest request = new OrganizationRequest();

		AuditDTO audit = new AuditDTO();
		ValidationResult invalidResult = new ValidationResult();
		invalidResult.setValid(false);
		invalidResult.setMessage("Validation failed");
		invalidResult.setStatus(org.springframework.http.HttpStatus.BAD_REQUEST);

		when(jwtService.extractSubject("valid-token")).thenReturn("user-123");
		when(organizationValidationStrategy.validateCreation(any())).thenReturn(invalidResult);
		when(auditService.createAuditDTO(any(), any(), any())).thenReturn(audit);

		mockMvc.perform(post("/api/orgs").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Validation failed"));
	}

	@Test
	void createOrganization_serviceException() throws Exception {
		OrganizationRequest request = new OrganizationRequest();

		AuditDTO audit = new AuditDTO();
		ValidationResult validResult = new ValidationResult();
		validResult.setValid(true);

		when(jwtService.extractSubject("valid-token")).thenReturn("user-123");
		when(organizationValidationStrategy.validateCreation(any())).thenReturn(validResult);
		when(organizationService.createOrganization(any(), any()))
				.thenThrow(new OrganizationServiceException("Internal error"));
		when(auditService.createAuditDTO(any(), any(), any())).thenReturn(audit);

		mockMvc.perform(post("/api/orgs").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Internal error"));
	}

	@Test
	void retrieveActiveOrganizationList_success_withData() throws Exception {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setPage(1);
		searchRequest.setSize(10);

		OrganizationDTO dto = new OrganizationDTO();
		dto.setOrganizationName("Test Org");

		List<OrganizationDTO> dtoList = List.of(dto);
		Map<Long, List<OrganizationDTO>> resultMap = Map.of(1L, dtoList);

		AuditDTO auditDTO = new AuditDTO();

		when(auditService.createAuditDTO(any(), any(), any())).thenReturn(auditDTO);
		when(organizationService.retrievePaginatedActiveOrganizationList(any())).thenReturn(resultMap);

		mockMvc.perform(get("/api/orgs").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token").param("page", "1")
				.param("size", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].organizationName").value("Test Org"));
	}

	@Test
	void retrieveActiveOrganizationList_success_emptyData() throws Exception {
		AuditDTO auditDTO = new AuditDTO();

		Map<Long, List<OrganizationDTO>> emptyMap = Map.of(0L, new ArrayList<>());

		when(auditService.createAuditDTO(any(), any(), any())).thenReturn(auditDTO);
		when(organizationService.retrievePaginatedActiveOrganizationList(any())).thenReturn(emptyMap);

		mockMvc.perform(get("/api/orgs").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token").param("page", "1")
				.param("size", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray()).andExpect(jsonPath("$.data").isEmpty());
	}
	

	@Test
	void retrieveActiveOrganizationList_exceptionThrown() throws Exception {
		AuditDTO auditDTO = new AuditDTO();

		when(auditService.createAuditDTO(any(), any(), any())).thenReturn(auditDTO);
		when(organizationService.retrievePaginatedActiveOrganizationList(any()))
				.thenThrow(new OrganizationServiceException("Database error"));

		mockMvc.perform(get("/api/orgs").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token").param("page", "1")
				.param("size", "10")).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.message").value("Database error"));
	}
	
	@Test
	void shouldReturnErrorResponseWhenValidationFails() throws Exception {
		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(false);
		validationResult.setMessage("Access Denied");
		validationResult.setStatus(HttpStatus.FORBIDDEN);
		String authorizationHeader = "Bearer fake-jwt-token";
		String orgId = "ORG123";

		when(organizationValidationStrategy.validateObject(orgId, authorizationHeader)).thenReturn(validationResult);

		mockMvc.perform(
				get("/api/orgs/my-organization").header("Authorization", authorizationHeader).header("X-Org-Id", orgId))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Access Denied"));
	}
	
	@Test
    void shouldReturnOrganizationWhenValidationPasses() throws Exception {
		String authorizationHeader = "Bearer fake-jwt-token";
		String orgId = "ORG123";
		
        ValidationResult validationResult = new ValidationResult();
        validationResult.setValid(true);

        OrganizationDTO orgDTO = new OrganizationDTO();
        orgDTO.setOrganizationName("TestOrg");

        when(organizationValidationStrategy.validateObject(orgId, authorizationHeader))
            .thenReturn(validationResult);

        when(organizationService.findByOrganizationId(orgId))
            .thenReturn(orgDTO);

        // Act & Assert
        mockMvc.perform(get("/api/orgs/my-organization")
                .header("Authorization", authorizationHeader)
                .header("X-Org-Id", orgId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("TestOrg is found."))
            .andExpect(jsonPath("$.data.organizationName").value("TestOrg"))
            .andDo(print());
    }

 
	@Test
	void shouldReturnInternalServerErrorWhenExceptionThrown() throws Exception {

		String authorizationHeader = "Bearer fake-jwt-token";
		String orgId = "ORG123";

		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);

		when(organizationValidationStrategy.validateObject(orgId, authorizationHeader)).thenReturn(validationResult);

		when(organizationService.findByOrganizationId(orgId)).thenThrow(new OrganizationServiceException("Unexpected error"));

		mockMvc.perform(
				get("/api/orgs/my-organization").header("Authorization", authorizationHeader).header("X-Org-Id", orgId))
				.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Unexpected error"));
	}
    
    
    @Test
    void updateOrganization_success() throws Exception {
        String orgId = "ORG123";
        String userId = "user-001";

        OrganizationRequest request = new OrganizationRequest();
        request.setOrganizationName("Updated Org");

        OrganizationDTO responseDto = new OrganizationDTO();
        responseDto.setOrganizationId(orgId);
        responseDto.setOrganizationName("Updated Org");

        ValidationResult valid = new ValidationResult();
        valid.setValid(true);

        when(jwtService.extractSubject(anyString())).thenReturn(userId);
        when(auditService.createAuditDTO(any(), any(), any())).thenReturn(new AuditDTO());
        when(organizationValidationStrategy.validateUpdating(any())).thenReturn(valid);
        when(organizationService.updateOrganization(any(), eq(userId), eq(orgId))).thenReturn(responseDto);

        mockMvc.perform(put("/api/orgs")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.organizationName").value("Updated Org"));
    }

    @Test
    void updateOrganization_validationFails() throws Exception {
        String orgId = "ORG123";

        OrganizationRequest request = new OrganizationRequest();
        request.setOrganizationName("Invalid Org");

        ValidationResult invalid = new ValidationResult();
        invalid.setValid(false);
        invalid.setMessage("Invalid data");
        invalid.setStatus(HttpStatus.BAD_REQUEST);

        when(jwtService.extractSubject(anyString())).thenReturn("user-001");
        when(auditService.createAuditDTO(any(), any(), any())).thenReturn(new AuditDTO());
        when(organizationValidationStrategy.validateUpdating(any())).thenReturn(invalid);

        mockMvc.perform(put("/api/orgs")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid data"));
    }

    @Test
    void updateOrganization_exceptionThrown() throws Exception {
        String orgId = "ORG123";

        OrganizationRequest request = new OrganizationRequest();
        request.setOrganizationName("Crashing Org");

        ValidationResult valid = new ValidationResult();
        valid.setValid(true);

        when(jwtService.extractSubject(anyString())).thenReturn("user-001");
        when(auditService.createAuditDTO(any(), any(), any())).thenReturn(new AuditDTO());
        when(organizationValidationStrategy.validateUpdating(any())).thenReturn(valid);
        when(organizationService.updateOrganization(any(), anyString(), anyString()))
                .thenThrow(new OrganizationServiceException("Database error"));

        mockMvc.perform(put("/api/orgs")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Database error"));
    }
    
    
    
    @Test
    void getOrganizationListByUserId_success_withData() throws Exception {
        String token = "Bearer valid.jwt.token";
        String userId = "user-123";

        OrganizationDTO orgDTO = new OrganizationDTO();
        orgDTO.setOrganizationId("ORG001");
        orgDTO.setOrganizationName("Test Org");

        List<OrganizationDTO> orgList = List.of(orgDTO);
        Map<Long, List<OrganizationDTO>> resultMap = Map.of(1L, orgList);

        when(jwtService.extractSubject("valid.jwt.token")).thenReturn(userId);
        when(auditService.createAuditDTO(any(), any(), any())).thenReturn(new AuditDTO());
        when(organizationService.findActiveOrganizationListByUserId(eq(userId), any())).thenReturn(resultMap);

        mockMvc.perform(get("/api/orgs/users")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].organizationName").value("Test Org"))
                .andExpect(jsonPath("$.totalRecord").value(1));
    }

    @Test
    void getOrganizationListByUserId_success_emptyData() throws Exception {
        String token = "Bearer valid.jwt.token";
        String userId = "user-123";

        Map<Long, List<OrganizationDTO>> resultMap = Map.of(0L, List.of());

        when(jwtService.extractSubject("valid.jwt.token")).thenReturn(userId);
        when(auditService.createAuditDTO(any(), any(), any())).thenReturn(new AuditDTO());
        when(organizationService.findActiveOrganizationListByUserId(eq(userId), any())).thenReturn(resultMap);

        mockMvc.perform(get("/api/orgs/users")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("No Active Organization List by this user."));
    }

    @Test
    void getOrganizationListByUserId_exceptionThrown() throws Exception {
        String token = "Bearer invalid.jwt.token";
        String userId = "user-123";

        when(jwtService.extractSubject(anyString())).thenReturn(userId);
        when(auditService.createAuditDTO(any(), any(), any())).thenReturn(new AuditDTO());
        when(organizationService.findActiveOrganizationListByUserId(eq(userId), any()))
                .thenThrow(new OrganizationServiceException("Internal failure"));

        mockMvc.perform(get("/api/orgs/users")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal failure"));
    }
}
