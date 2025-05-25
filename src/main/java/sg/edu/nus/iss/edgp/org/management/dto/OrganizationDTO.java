package sg.edu.nus.iss.edgp.org.management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationDTO {

	private String organizationId;
	private String organizationName;
	private String address;
	private String contactNumber;
	private String uniqueEntityNumber;
	private String streetAddress;
	private String city;
	private String postalCode;
	private String country;
	private String websiteURL;
	private int organizationSize;
    private SectorDTO sectorDTO;
    private String primaryContactName;
    private String primaryContactPosition;
    private String primaryContactEmail;
    private String primaryContactNumber;
    private String remark;
    private Boolean active;
}
