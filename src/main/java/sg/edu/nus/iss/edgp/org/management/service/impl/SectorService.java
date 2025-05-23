package sg.edu.nus.iss.edgp.org.management.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.service.ISectorService;
import sg.edu.nus.iss.edgp.org.management.utility.DTOMapper;

@Service
@RequiredArgsConstructor
public class SectorService implements ISectorService {

	private static final Logger logger = LoggerFactory.getLogger(SectorService.class);
	private final SectorRepository sectorRepository;

	@Override
	public SectorDTO createSector(SectorRequest sectorReq) throws Exception {
		Sector sector = new Sector();
		sector.setSectorName(sectorReq.getSectorName());
		sector.setSectorCode(sectorReq.getSectorCode());
		sector.setDescription(sector.getDescription());
		sector.setCreatedBy(sectorReq.getCreatedBy());
		sector.setLastUpdatedBy(sector.getCreatedBy());
		sector.setLastUpdatedDateTime(LocalDateTime.now());
		Sector createdSector = sectorRepository.save(sector);
		logger.info("Creating sector ....");
		return DTOMapper.toSectorDTO(createdSector);

	}

	public List<Sector> findBySectorNameAndCode(String sectorName, String sectorCode) {
		try {
			return sectorRepository.findBySectorNameOrSectorCode(sectorName, sectorCode);
		} catch (Exception ex) {
			logger.error("Exception occurred while executing findBySectorNameAndCode", ex);
			throw ex;
		}

	}

}
