package sg.edu.nus.iss.edgp.org.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        mockMvc.perform(post("/api/orgs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
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

        mockMvc.perform(post("/api/orgs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
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

        mockMvc.perform(post("/api/orgs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal error"));
    }
}
