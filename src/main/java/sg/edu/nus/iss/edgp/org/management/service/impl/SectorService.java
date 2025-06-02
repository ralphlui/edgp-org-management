package sg.edu.nus.iss.edgp.org.management.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorRequest;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;
import sg.edu.nus.iss.edgp.org.management.exception.SectorServiceException;
import sg.edu.nus.iss.edgp.org.management.repository.SectorRepository;
import sg.edu.nus.iss.edgp.org.management.service.ISectorService;
import sg.edu.nus.iss.edgp.org.management.utility.DTOMapper;

@Service
@RequiredArgsConstructor
public class SectorService implements ISectorService {

	private static final Logger logger = LoggerFactory.getLogger(SectorService.class);
	private final SectorRepository sectorRepository;

	@Override
	public SectorDTO createSector(SectorRequest sectorReq, String userId) {
		try {

			Sector sector = new Sector();
			sector.setSectorName(sectorReq.getSectorName());
			sector.setSectorCode(sectorReq.getSectorCode());
			sector.setDescription(sector.getDescription());
			sector.setCreatedBy(userId);
			sector.setLastUpdatedBy(userId);
			sector.setLastUpdatedDateTime(LocalDateTime.now());
			sector.setRemark(sectorReq.getRemark());
			Sector createdSector = sectorRepository.save(sector);
			logger.info("Creating sector ....");
			return DTOMapper.toSectorDTO(createdSector);
		} catch (Exception ex) {
			logger.error("Exception occurred while creating sector", ex);
			throw new SectorServiceException("An error occured while creating sector", ex);
		}

	}

	public List<Sector> findBySectorNameAndCode(String sectorName, String sectorCode) {
		try {
			return sectorRepository.findBySectorNameOrSectorCode(sectorName, sectorCode);
		} catch (Exception ex) {
			logger.error("Exception occurred while searching for the sector by name and code", ex);
			throw new SectorServiceException("An error occurred while searching for the sector by name and code", ex);
		}

	}

	@Override
	public Map<Long, List<SectorDTO>> retrieveActiveSectorList(Pageable pageable) {
		try {
			List<SectorDTO> sectorDTOList = new ArrayList<>();
			Page<Sector> sectorPages = sectorRepository.findActiveSectorList(true, pageable);
			long totalRecord = sectorPages.getTotalElements();
			if (totalRecord > 0) {
				logger.info("Active sector list is found.");
				for (Sector sector : sectorPages.getContent()) {
					SectorDTO sectorDTO = DTOMapper.toSectorDTO(sector);
					sectorDTOList.add(sectorDTO);
				}
			}
			Map<Long, List<SectorDTO>> result = new HashMap<>();
			result.put(totalRecord, sectorDTOList);
			return result;

		} catch (Exception ex) {
			logger.error("Exception occurred while retrieving active sector list", ex);
			throw new SectorServiceException("An error occurred while retrieving active sector list", ex);

		}
	}

	@Override
	public SectorDTO updateSector(SectorRequest sectorReq, String userId, String sectorId) {
		try {
			
			Optional<Sector> sectorResult = sectorRepository.findBySectorId(sectorId);
			if (!sectorResult.isPresent()) {
				throw new SectorServiceException("No matching sector found");
			}
			Sector dbSector = sectorResult.get();
			dbSector.setDescription(sectorReq.getDescription());
			dbSector.setLastUpdatedBy(userId);
			dbSector.setLastUpdatedDateTime(LocalDateTime.now());
			dbSector.setRemark(sectorReq.getRemark());
			dbSector.setActive(sectorReq.getActive());
			logger.info("Updating Sector...");
			Sector updatedSector = sectorRepository.save(dbSector);
			logger.info("Sector is updated successfully.");
			return DTOMapper.toSectorDTO(updatedSector);
		} catch (Exception ex) {
			logger.error("Exception occurred while updating sector", ex);
			throw new SectorServiceException("An error occurred while updating sector", ex);
		}
	}
	
	@Override
	public SectorDTO findBySectorId(String sectorId) {
		try {
			Optional<Sector>  sector = sectorRepository.findBySectorId(sectorId);
			if (sector.isPresent()) {
				return DTOMapper.toSectorDTO(sector.get());
			}
			throw new SectorServiceException("Unable to find active store with this sector");
			
		} catch (Exception e) {
			logger.error("Exception occurred while searching fot the sector by sector id", e);
			throw new SectorServiceException("An error occurred while searching fot the sector by sector id", e);
		}
		
	}
	
	@Override
	public Sector findBySectorIdAndIsActive(String sectorId) {
		try {
			return sectorRepository.findBySectorIdAndIsActive(sectorId, true);
		} catch (Exception e) {
			logger.error("Exception occurred while searching fot the active sector by sector id", e);
			throw new SectorServiceException("An error occurred while searching fot the active sector by sector id", e);
		}
		
	}
	
	

}
