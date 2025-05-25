package sg.edu.nus.iss.edgp.org.management.service;

import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.entity.Organization;

public interface IOrganizationService {

	OrganizationDTO createOrganization(OrganizationRequest orgReq, String userId);
	
	Organization findByOrganizationName(String organizationName);
	
	Organization findByUEN(String uniqueEntityNumber); 
}
