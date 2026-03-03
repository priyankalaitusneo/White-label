package com.laitsneo.whitelbl.entity.Admin;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vendors {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 50)
    private String id;

    @NotBlank(message = "Vendor name is required")
    @Column(name = "vendor_name", nullable = false, unique = true)
    private String vendorName;

    @NotBlank(message = "API is required")
    @Column(name = "api", nullable = false)
    private String api;

    @NotNull(message = "Charges are required")
    @Column(name = "charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal charges;

    @NotNull(message = "Amount is required")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "Active";

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @PrePersist
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            String date = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String random = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();

            this.id = "VND-" + date + "-" + random;
        }
    }
    
}