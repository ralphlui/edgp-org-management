package sg.edu.nus.iss.edgp.org.management.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	
	private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

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
			throw new OrganizationServiceException("An error occured while creating organization", ex);
		}
	}
	
	
	@Override
	public Organization findByOrganizationName(String organizationName) {
		try {
			return organizationRepository.findByOrganizationName(organizationName);
		} catch (Exception ex) {
			logger.error("Exception occurred while searching for the organization by name", ex);
			throw new OrganizationServiceException("An error occurred while searching for the organization by name", ex);
		}
	}
	
	@Override
	public Organization findByUEN(String uniqueEntityNumber) {
		try {
			return organizationRepository.findByUniqueEntityNumber(uniqueEntityNumber);
		} catch (Exception ex) {
			logger.error("Exception occurred while executing searching organization by UEN", ex);
			throw new OrganizationServiceException("An error occurred while executing searching organization by UEN", ex);
		}
	}
	
	@Override
	public Map<Long, List<OrganizationDTO>> retrievePaginatedActiveOrganizationList(Pageable pageable) {
		try {
			List<OrganizationDTO> organizationDTOList = new ArrayList<>();
			Page<Organization> organizationPages = organizationRepository.findPaginatedActiveOrganizationList(true, pageable);
			long totalRecord = organizationPages.getTotalElements();
			if (totalRecord > 0) {
				logger.info("Active paginated organization list is found.");
				for (Organization organization : organizationPages.getContent()) {
					OrganizationDTO organizationDTO = DTOMapper.toOrganizationDTO(organization);
					organizationDTOList.add(organizationDTO);
				}
			}
			Map<Long, List<OrganizationDTO>> result = new HashMap<>();
			result.put(totalRecord, organizationDTOList);
			return result;

		} catch (Exception ex) {
			logger.error("Exception occurred while retrieving paginated active organization list", ex);
			throw new OrganizationServiceException("An error occurred while retrieving paginated active organization list", ex);

		}
	}
	
	@Override
	public Map<Long, List<OrganizationDTO>> retrieveActiveOrganizationList() {
		try {
			List<OrganizationDTO> organizationDTOList = new ArrayList<>();
			List<Organization> organizationList = organizationRepository.findActiveOrganizationList(true);
			long totalRecord = organizationList.size();
			if (totalRecord > 0) {
				logger.info("Active organization list without pagination is found.");
				for (Organization organization : organizationList) {
					OrganizationDTO organizationDTO = DTOMapper.toOrganizationDTO(organization);
					organizationDTOList.add(organizationDTO);
				}
			}
			Map<Long, List<OrganizationDTO>> result = new HashMap<>();
			result.put(totalRecord, organizationDTOList);
			return result;

		} catch (Exception ex) {
			logger.error("Exception occurred while retrieving active organization list without pagination", ex);
			throw new OrganizationServiceException("An error occurred while retrieving active organization list without pagination", ex);

		}
	}
	
	@Override
	public OrganizationDTO findByOrganizationId(String organizationId) {
		try {
			Organization organization = organizationRepository.findByOrganizationId(organizationId);
			OrganizationDTO orgDTO = DTOMapper.toOrganizationDTO(organization);
			return orgDTO;

		} catch (Exception ex) {
			logger.error("Exception occurred while searching for the organization by org id", ex);
			throw new OrganizationServiceException("An error occurred while searching for the organization by org id",
					ex);
		}
	}
	
	
	@Override
	public OrganizationDTO updateOrganization(OrganizationRequest orgReq, String userId, String organizationId) {
		try {
			
			Organization dbOrganization = organizationRepository.findByOrganizationId(organizationId);
			if (dbOrganization == null) {
				throw new OrganizationServiceException("No matching organization found");
			}
			dbOrganization.setAddress(orgReq.getAddress());
			dbOrganization.setContactNumber(orgReq.getContactNumber());
			dbOrganization.setStreetAddress(orgReq.getStreetAddress());
			dbOrganization.setCity(orgReq.getCity());
			dbOrganization.setPostalCode(orgReq.getPostalCode());
			dbOrganization.setCountry(orgReq.getCountry());
			dbOrganization.setWebsiteURL(orgReq.getWebsiteURL());
			dbOrganization.setOrganizationSize(orgReq.getOrganizationSize());
			Sector sector = sectorRepository.findById(orgReq.getSector().getSectorId())
				    .orElseThrow(() ->  new OrganizationServiceException("Sector not found with this name : " + orgReq.getSector().getSectorName()));
			dbOrganization.setSector(sector);
			dbOrganization.setPrimaryContactName(orgReq.getPrimaryContactName());
			dbOrganization.setPrimaryContactPosition(orgReq.getPrimaryContactPosition());
			dbOrganization.setPrimaryContactEmail(orgReq.getPrimaryContactEmail());
			dbOrganization.setPrimaryContactNumber(orgReq.getPrimaryContactNumber());
			dbOrganization.setLastUpdatedBy(userId);
			dbOrganization.setLastUpdatedDateTime(LocalDateTime.now());
			dbOrganization.setRemark(orgReq.getRemark());
			logger.info("Updating Organization...");
			Organization createdOrganization = organizationRepository.save(dbOrganization);
			logger.info("Organization is updated successfully.");
			return DTOMapper.toOrganizationDTO(createdOrganization);
		} catch (Exception ex) {
			logger.error("Exception occurred while updating organization", ex);
			throw new OrganizationServiceException("An error occurred while updating organization", ex);
		}
	}
	
	@Override
	public Map<Long, List<OrganizationDTO>> findActiveOrganizationListByUserId(String userId,
			Pageable pageable) {
		try {
			
			List<OrganizationDTO> organizationDTOList = new ArrayList<>();
			Page<Organization> organizationPages = organizationRepository.findOrganizationListByUserId(userId, true, pageable);
			long totalRecord = organizationPages.getTotalElements();
			if (totalRecord > 0) {
				logger.info("Active organization list by user id is found.");
				for (Organization organization : organizationPages.getContent()) {
					OrganizationDTO organizationDTO = DTOMapper.toOrganizationDTO(organization);
					organizationDTOList.add(organizationDTO);
				}
			}
			Map<Long, List<OrganizationDTO>> result = new HashMap<>();
			logger.info("Total record of retrieving organization list by user id.. {}", totalRecord);
			result.put(totalRecord, organizationDTOList);
			return result;
			
		} catch (Exception ex) {
			logger.error("Exception occurred while retrieving organization list by user id", ex);
			throw new OrganizationServiceException("An error occurred while retrieving organization list by user id", ex);

		}

	}

}
