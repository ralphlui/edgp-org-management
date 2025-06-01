package sg.edu.nus.iss.edgp.org.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sg.edu.nus.iss.edgp.org.management.dto.*;
import sg.edu.nus.iss.edgp.org.management.exception.SectorServiceException;
import sg.edu.nus.iss.edgp.org.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.org.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.org.management.service.impl.SectorService;
import sg.edu.nus.iss.edgp.org.management.strategy.impl.SectorValidationStrategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

@WebMvcTest(SectorController.class)
@AutoConfigureMockMvc(addFilters = false)
class SectorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private SectorService sectorService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private AuditService auditService;

	@MockitoBean
	private SectorValidationStrategy sectorValidationStrategy;

	private static final String BEARER_TOKEN = "Bearer faketoken123";

	@Test
	@WithMockUser(authorities = "SCOPE_sector.manage")
	void createSector_success() throws Exception {

		SectorRequest request = new SectorRequest();
		SectorDTO responseDto = new SectorDTO();
		ValidationResult validResult = new ValidationResult();
		validResult.setValid(true);

		when(jwtService.extractSubject("faketoken123")).thenReturn("user123");
		when(sectorValidationStrategy.validateCreation(any())).thenReturn(validResult);
		when(sectorService.createSector(any(), any())).thenReturn(responseDto);

		mockMvc.perform(post("/api/orgs/sectors").header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Success! The sector has been added.")).andDo(print());
	}

	@Test
	@WithMockUser(authorities = "SCOPE_sector.manage")
	void createSector_validationFailure() throws Exception {
		SectorRequest request = new SectorRequest();
		ValidationResult invalidResult = new ValidationResult();
		invalidResult.setValid(false);
		invalidResult.setStatus(HttpStatus.BAD_REQUEST);
		invalidResult.setMessage("Invalid input");

		when(jwtService.extractSubject("faketoken123")).thenReturn("user123");
		when(sectorValidationStrategy.validateCreation(any())).thenReturn(invalidResult);

		mockMvc.perform(post("/api/orgs/sectors").header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid input"))
				.andDo(print());
	}

	@Test
	@WithMockUser(authorities = "SCOPE_sector.manage")
	void createSector_sectorServiceException_returns500() throws Exception {
		SectorRequest request = new SectorRequest();

		ValidationResult validResult = new ValidationResult();
		validResult.setValid(true);

		when(jwtService.extractSubject("faketoken123")).thenReturn("user123");
		when(sectorValidationStrategy.validateCreation(any())).thenReturn(validResult);
		when(sectorService.createSector(any(), any())).thenThrow(new SectorServiceException("Something went wrong"));

		// Act & Assert
		mockMvc.perform(post("/api/orgs/sectors").header("Authorization", BEARER_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("Something went wrong"));
	}

	@Test
	@WithMockUser(authorities = "SCOPE_sector.manage")
	void retrieveActiveSectorList_success_withData() throws Exception {
		SectorDTO dto = new SectorDTO(); // mock fields if needed
		List<SectorDTO> sectorList = List.of(dto);
		Map<Long, List<SectorDTO>> resultMap = Map.of(1L, sectorList);

		when(sectorService.retrieveActiveSectorList(any(Pageable.class))).thenReturn(resultMap);

		mockMvc.perform(get("/api/orgs/sectors").header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN).param("page", "1")
				.param("size", "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Successfully retrieved all active sectors."))
				.andExpect(jsonPath("$.data").isArray()).andExpect(jsonPath("$.totalRecord").value(1));
	}

	@Test
	@WithMockUser(authorities = "SCOPE_sector.manage")
	void retrieveActiveSectorList_success_emptyList() throws Exception {
		Map<Long, List<SectorDTO>> resultMap = Map.of(0L, List.of());

		when(sectorService.retrieveActiveSectorList(any(Pageable.class))).thenReturn(resultMap);

		mockMvc.perform(get("/api/orgs/sectors").header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN).param("page", "1")
				.param("size", "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("No Active Sector List."))
				.andExpect(jsonPath("$.data").isArray()).andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	@WithMockUser(authorities = "SCOPE_sector.manage")
	void retrieveActiveSectorList_serviceException_returns500() throws Exception {
		when(sectorService.retrieveActiveSectorList(any(Pageable.class)))
				.thenThrow(new SectorServiceException("Service failed"));

		mockMvc.perform(get("/api/orgs/sectors").header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN).param("page", "1")
				.param("size", "10").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.message").value("Service failed"));
	}
}
