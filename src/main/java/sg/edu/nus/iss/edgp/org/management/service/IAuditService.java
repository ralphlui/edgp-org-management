package sg.edu.nus.iss.edgp.org.management.service;

import sg.edu.nus.iss.edgp.org.management.dto.AuditDTO;

public interface IAuditService {
	void sendMessage(AuditDTO autAuditDTO, String authorizationHeader);

}
