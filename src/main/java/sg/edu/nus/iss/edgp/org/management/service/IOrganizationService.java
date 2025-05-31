package sg.edu.nus.iss.edgp.org.management.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.entity.Organization;

public interface IOrganizationService {

	OrganizationDTO createOrganization(OrganizationRequest orgReq, String userId);
	
	Organization findByOrganizationName(String organizationName);
	
	Organization findByUEN(String uniqueEntityNumber);
	
	 Map<Long, List<OrganizationDTO>> retrieveActiveOrganizationList(Pageable pageable);
	 
	 OrganizationDTO findByOrganizationId(String organizationId);
	 
	 OrganizationDTO updateOrganization(OrganizationRequest orgReq, String userId, String organizationId); 
	 
	 Map<Long, List<OrganizationDTO>> findActiveOrganizationListByUserId(String userId,
				Pageable pageable);
}
