package com.laitsneo.whitelbl.entity.Admin;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Charges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int slNo;

    @NotBlank(message = "UserId is required")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull(message = "FromRange is required")
    @PositiveOrZero(message = "FromRange must be >= 0")
    private Double fromRange;

    @NotNull(message = "ToRange is required")
    @Positive(message = "ToRange must be > 0")
    private Double toRange;

    @NotBlank(message = "ChargesType is required")
    @Column(name = "charges_type", nullable = false, length = 50)
    private String chargesType;

    @NotNull(message = "Charges is required")
    @Positive(message = "Charges must be > 0")
    private Double charges;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
