package sg.edu.nus.iss.edgp.org.management.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Sector {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.AUTO)
	private String sectorId;

	@Column(nullable = false, unique = true)
	private String sectorName;

	@Column(nullable = false, unique = true)
	private String sectorCode;

	@Column(nullable = true)
	private String description;

	@Column(nullable = false, columnDefinition = "datetime")
	private LocalDateTime createdDateTime = LocalDateTime.now();;

	@Column(nullable = false)
	private String createdBy;

	@Column(nullable = false, columnDefinition = "datetime")
	private LocalDateTime lastUpdatedDateTime;

	@Column(nullable = false)
	private String lastUpdatedBy;

	@Column(nullable = true)
	private String remark;
	
	@Column(nullable = false, columnDefinition = "boolean default true")
	private boolean isActive;
}
