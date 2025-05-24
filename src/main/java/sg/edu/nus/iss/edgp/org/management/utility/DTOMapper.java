package sg.edu.nus.iss.edgp.org.management.utility;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
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
}
