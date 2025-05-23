package sg.edu.nus.iss.edgp.org.management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectorDTO {

	private String sectorID;
	private String sectorName;
	private String sectorCode;
	private String description;
	private String createdBy;
	private String lastUpdatedBy;
	private String remark;
}
