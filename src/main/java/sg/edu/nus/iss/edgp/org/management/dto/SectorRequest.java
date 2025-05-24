package sg.edu.nus.iss.edgp.org.management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectorRequest {

	private String sectorId;
	private String sectorName;
	private String sectorCode;
	private String description;
	private String createdBy;
	private String lastUpdatedBy;
	private String remark;
	private Boolean active = true;
}