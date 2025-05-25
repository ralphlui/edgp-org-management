package sg.edu.nus.iss.edgp.org.management.utility;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.entity.Organization;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;

@Component
public class DTOMapper {

	public static SectorDTO toSectorDTO(Sector sector) {
		SectorDTO sectorDTO = new SectorDTO();
		sectorDTO.setSectorID(sector.getSectorId());
		sectorDTO.setSectorName(sector.getSectorName());
		sectorDTO.setSectorCode(sector.getSectorCode());
		sectorDTO.setCreatedBy(sector.getCreatedBy());
		sectorDTO.setLastUpdatedBy(sector.getLastUpdatedBy());
		sectorDTO.setRemark(sector.getRemark());
		sectorDTO.setDescription(sector.getDescription());
		sectorDTO.setActive(sector.isActive());
		return sectorDTO;
	}
	
	public static OrganizationDTO toOrganizationDTO(Organization organization) {
		OrganizationDTO organizationDTO = new OrganizationDTO();
		organizationDTO.setOrganizationId(organization.getOrganizationId());
		organizationDTO.setOrganizationName(organization.getOrganizationName());
		organizationDTO.setAddress(organization.getAddress());
		organizationDTO.setContactNumber(organization.getContactNumber());
		organizationDTO.setUniqueEntityNumber(organization.getUniqueEntityNumber());
		organizationDTO.setStreetAddress(organization.getStreetAddress());
		organizationDTO.setCity(organization.getCity());
		organizationDTO.setPostalCode(organization.getPostalCode());
		organizationDTO.setCountry(organization.getCountry());
		organizationDTO.setWebsiteURL(organization.getWebsiteURL());
		organizationDTO.setOrganizationSize(organization.getOrganizationSize());
		organizationDTO.setSector(toSectorDTO(organization.getSector()));
		organizationDTO.setPrimaryContactName(organization.getPrimaryContactName());
		organizationDTO.setPrimaryContactPosition(organization.getPrimaryContactPosition());
		organizationDTO.setPrimaryContactNumber(organization.getPrimaryContactNumber());
		organizationDTO.setRemark(organization.getRemark());
		organizationDTO.setActive(organization.isActive());
		return organizationDTO;
	}
	
	public static SectorDTO mapStoreToResult(Sector sector) {
		SectorDTO sectorDTO = new SectorDTO();
		sectorDTO.setSectorCode(sector.getSectorCode());
		sectorDTO.setSectorName(sector.getSectorName());
		sectorDTO.setSectorID(sector.getSectorId());
		return sectorDTO;
	}
}
