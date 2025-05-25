package sg.edu.nus.iss.edgp.org.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sg.edu.nus.iss.edgp.org.management.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, String> {

	Organization findByOrganizationName(String organizationName);
	
	Organization findByUniqueEntityNumber(String uniqueEntityNumber);
	
	@Query("SELECT org FROM Organization org WHERE org.isActive = ?1")
	Page<Organization> findActiveOrganizationList(boolean isActive, Pageable pageable);
	
	Organization findByOrganizationId(String organizationId);
}
