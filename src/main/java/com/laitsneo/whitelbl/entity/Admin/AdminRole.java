package com.laitsneo.whitelbl.entity.Admin;

import java.util.Date;

import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRole {

    @Id
    private String adminId;
   

    private String userId;   // User ID (U001)

    @Column(nullable = false)
    private String userName;  // John Doe

    @Column(nullable = false)
    private String email;     // john@finverse.com

    @Column(nullable = false)
    private String roleId;

    private String permissions;

    private String status; // ACTIVE / INACTIVE

    private Boolean deleted = false;

    @CreatedDate
    private Date createdAt = new Date();

    @UpdateTimestamp
    private Date updatedAt = new Date();


}
