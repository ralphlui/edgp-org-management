package sg.edu.nus.iss.edgp.org.management.service;

import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;

public interface ISectorService {

	SectorDTO createSector(SectorRequest sectorReq) throws Exception;
}
