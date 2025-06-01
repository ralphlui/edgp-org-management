package sg.edu.nus.iss.edgp.org.management.utility;

import org.junit.jupiter.api.Test;

import sg.edu.nus.iss.edgp.org.management.dto.OrganizationDTO;
import sg.edu.nus.iss.edgp.org.management.dto.SectorDTO;
import sg.edu.nus.iss.edgp.org.management.entity.Organization;
import sg.edu.nus.iss.edgp.org.management.entity.Sector;

import static org.junit.jupiter.api.Assertions.*;

class DTOMapperTest {

    @Test
    void testToSectorDTO() {
        Sector sector = new Sector();
        sector.setSectorId("SEC001");
        sector.setSectorName("Finance");
        sector.setSectorCode("FIN");
        sector.setCreatedBy("admin");
        sector.setLastUpdatedBy("admin");
        sector.setRemark("Top sector");
        sector.setDescription("Handles financial institutions");
        sector.setActive(true);

        SectorDTO dto = DTOMapper.toSectorDTO(sector);

        assertEquals("SEC001", dto.getSectorID());
        assertEquals("Finance", dto.getSectorName());
        assertEquals("FIN", dto.getSectorCode());
        assertEquals("admin", dto.getCreatedBy());
        assertTrue(dto.getActive());
        assertEquals("Top sector", dto.getRemark());
        assertEquals("Handles financial institutions", dto.getDescription());
    }

    @Test
    void testMapStoreToResult() {
        Sector sector = new Sector();
        sector.setSectorId("SEC002");
        sector.setSectorName("Healthcare");
        sector.setSectorCode("HEA");

        SectorDTO dto = DTOMapper.mapStoreToResult(sector);

        assertEquals("SEC002", dto.getSectorID());
        assertEquals("Healthcare", dto.getSectorName());
        assertEquals("HEA", dto.getSectorCode());
    }

    @Test
    void testToOrganizationDTO() {
        Sector sector = new Sector();
        sector.setSectorId("SEC003");
        sector.setSectorName("Education");
        sector.setSectorCode("EDU");

        Organization org = new Organization();
        org.setOrganizationId("ORG123");
        org.setOrganizationName("Tech Uni");
        org.setAddress("123 College Rd");
        org.setContactNumber("654321");
        org.setUniqueEntityNumber("UEN999");
        org.setStreetAddress("Block A");
        org.setCity("Singapore");
        org.setPostalCode("999999");
        org.setCountry("SG");
        org.setWebsiteURL("https://techuni.edu");
        org.setOrganizationSize(30);
        org.setSector(sector);
        org.setPrimaryContactName("Alice");
        org.setPrimaryContactPosition("Director");
        org.setPrimaryContactNumber("12345678");
        org.setRemark("Trusted university");
        org.setActive(true);

        OrganizationDTO dto = DTOMapper.toOrganizationDTO(org);

        assertEquals("ORG123", dto.getOrganizationId());
        assertEquals("Tech Uni", dto.getOrganizationName());
        assertEquals("Education", dto.getSector().getSectorName());
        assertEquals("SG", dto.getCountry());
        assertTrue(dto.getActive());
        assertEquals("Alice", dto.getPrimaryContactName());
        assertEquals("Director", dto.getPrimaryContactPosition());
        assertEquals("https://techuni.edu", dto.getWebsiteURL());
    }
}

