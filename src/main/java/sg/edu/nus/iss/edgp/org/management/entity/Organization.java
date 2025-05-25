package sg.edu.nus.iss.edgp.org.management.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Organization {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.AUTO)
	private String organizationId;
	
	@Column(nullable = false, unique = true)
	private String organizationName;
	
	@Column(nullable = false)
	private String address;
	
	
	@Column(nullable = true)
	private String contactNumber;
	
	@Column(nullable = false, unique = true)
	private String uniqueEntityNumber;
	
	@Column(nullable = true)
	private String streetAddress;
	
	@Column(nullable = true)
	private String city;
	
	@Column(nullable = true)
	private String postalCode;
	
	@Column(nullable = true)
	private String country;
	
	@Column(nullable = true)
	private String websiteURL;
	
	@Column(nullable = true)
	private int organizationSize;
	
	@ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sectorId")
    private Sector Sector;
	
	@Column(nullable = false)
	private String primaryContactName;
	
	@Column(nullable = true)
	private String primaryContactPosition;
	
	@Column(nullable = false)
	private String primaryContactEmail;
	
	@Column(nullable = false)
	private String primaryContactNumber;
	
	@Column(nullable = false, columnDefinition = "datetime")
	private LocalDateTime createdDateTime = LocalDateTime.now();

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
