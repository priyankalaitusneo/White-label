package com.laitsneo.whitelbl.entity.Client;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "support_ticket")
@Data
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", unique = true)
    private String ticketId;

    private String userId;
    private String clientName;
    private String clientEmail;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;
    private String priority;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.ticketId = "TCKT" + System.currentTimeMillis();
        this.status = "OPEN";
        this.priority = "NORMAL";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
