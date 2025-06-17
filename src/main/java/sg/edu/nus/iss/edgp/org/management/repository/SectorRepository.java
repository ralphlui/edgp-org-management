package sg.edu.nus.iss.edgp.org.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sg.edu.nus.iss.edgp.org.management.entity.Sector;

public interface SectorRepository extends JpaRepository<Sector, String> {

	@Query("SELECT s FROM Sector s WHERE s.sectorName = ?1 OR s.sectorCode = ?2")
	List<Sector> findBySectorNameOrSectorCode(String sectorName, String sectorCode);
	
	@Query("SELECT s FROM Sector s WHERE s.isActive = ?1")
	Page<Sector> findPaginatedActiveSectorList(boolean isActive, Pageable pageable);
	
	Optional<Sector> findBySectorId(String sectorId);
	
	@Query("SELECT s FROM Sector s WHERE s.sectorId = ?1 AND s.isActive = ?2")
	Sector findBySectorIdAndIsActive(String sectorId, Boolean isActive);
	
	@Query("SELECT s FROM Sector s WHERE s.isActive = ?1")
	List<Sector> findActiveSectorList(boolean isActive);
}
