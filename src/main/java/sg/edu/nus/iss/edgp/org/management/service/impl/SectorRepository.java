package sg.edu.nus.iss.edgp.org.management.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sg.edu.nus.iss.edgp.org.management.entity.Sector;

public interface SectorRepository extends JpaRepository<Sector, String> {

	@Query("SELECT s FROM Sector s WHERE s.sectorName = ?1 OR s.sectorCode = ?2")
	List<Sector> findBySectorNameOrSectorCode(String sectorName, String sectorCode);

}
