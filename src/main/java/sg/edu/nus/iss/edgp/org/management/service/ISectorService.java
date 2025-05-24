package sg.edu.nus.iss.edgp.org.management.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;

public interface ISectorService {

	SectorDTO createSector(SectorRequest sectorReq, String userId) throws Exception;
	
    Map<Long, List<SectorDTO>> retrieveActiveSectorList(Pageable pageable);
    
    SectorDTO updateSector(SectorRequest sectorReq, String userId, String sectorId);
    
    Sector findBySectorId(String sectorId);
}
