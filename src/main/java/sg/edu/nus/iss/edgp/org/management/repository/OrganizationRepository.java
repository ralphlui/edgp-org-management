package sg.edu.nus.iss.edgp.org.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.edu.nus.iss.edgp.org.management.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, String> {

	Organization findByOrganizationName(String organizationName);
	
	Organization findByUniqueEntityNumber(String uniqueEntityNumber);
}
