package com.mippay.entity.Client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "authentication")
public class Authentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false,unique = true)
    private String userId;

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;  // auto-generate
    
    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret; // auto-generate

	@Column(name = "pg_id")
	private String pgId;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;


    public Authentication(String userId,String name, String clientId, String clientSecret) {
        this.userId = userId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientName = name;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
}
