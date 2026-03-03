package com.laitsneo.whitelbl.dto.Client;

import lombok.Data;

@Data
public class SupportTicketRequestDTO {

	private String subject;
    private String description;
    private String clientName;
    private String clientEmail;

}
