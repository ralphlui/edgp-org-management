package sg.edu.nus.iss.edgp.org.management.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.OrganizationRequest;
import sg.edu.nus.iss.edgp.org.management.entity.Organization;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.exception.OrganizationServiceException;
import sg.edu.nus.iss.edgp.org.management.repository.OrganizationRepository;
import sg.edu.nus.iss.edgp.org.management.repository.SectorRepository;
import sg.edu.nus.iss.edgp.org.management.service.IOrganizationService;
import sg.edu.nus.iss.edgp.org.management.utility.DTOMapper;

@Service
@RequiredArgsConstructor
public class OrganizationService implements IOrganizationService {
	
	private final  OrganizationRepository organizationRepository;
	private final  SectorRepository sectorRepository;

	
	private static final Logger logger = LoggerFactory.getLogger(SectorService.class);

	@Override
	public OrganizationDTO createOrganization(OrganizationRequest orgReq, String userId) {
		try {

			Organization organization = new Organization();
			organization.setOrganizationName(orgReq.getOrganizationName());
			organization.setAddress(orgReq.getAddress());
			organization.setContactNumber(orgReq.getContactNumber());
			organization.setUniqueEntityNumber(orgReq.getUniqueEntityNumber());
			organization.setStreetAddress(orgReq.getStreetAddress());
			organization.setCity(orgReq.getCity());
			organization.setPostalCode(orgReq.getPostalCode());
			organization.setCountry(orgReq.getCountry());
			organization.setWebsiteURL(orgReq.getWebsiteURL());
			organization.setOrganizationSize(orgReq.getOrganizationSize());
			Sector sector = sectorRepository.findById(orgReq.getSector().getSectorId())
				    .orElseThrow(() ->  new OrganizationServiceException("Sector not found with this name : " + orgReq.getSector().getSectorName()));
			organization.setSector(sector);
			organization.setPrimaryContactName(orgReq.getPrimaryContactName());
			organization.setPrimaryContactPosition(orgReq.getPrimaryContactPosition());
			organization.setPrimaryContactEmail(orgReq.getPrimaryContactEmail());
			organization.setPrimaryContactNumber(orgReq.getPrimaryContactNumber());
			organization.setCreatedBy(userId);
			organization.setLastUpdatedBy(userId);
			organization.setLastUpdatedDateTime(LocalDateTime.now());
			organization.setRemark(orgReq.getRemark());
			Organization createdOrganization = organizationRepository.save(organization);
			logger.info("Creating sector ....");
			return DTOMapper.toOrganizationDTO(createdOrganization);
		} catch (Exception ex) {
			logger.error("Exception occurred while creating organization", ex);
			throw new OrganizationServiceException("Failed during creating organization operation", ex);
		}
	}
	
	
	@Override
	public Organization findByOrganizationName(String organizationName) {
		try {
			return organizationRepository.findByOrganizationName(organizationName);
		} catch (Exception ex) {
			logger.error("Exception occurred while executing findByOrganizationName", ex);
			throw new OrganizationServiceException("Failed during searching organization by name operation", ex);
		}

	}
	
	@Override
	public Organization findByUEN(String uniqueEntityNumber) {
		try {
			return organizationRepository.findByUniqueEntityNumber(uniqueEntityNumber);
		} catch (Exception ex) {
			logger.error("Exception occurred while executing findByUEN", ex);
			throw new OrganizationServiceException("Failed during searching organization by UEN", ex);
		}

	}

}
