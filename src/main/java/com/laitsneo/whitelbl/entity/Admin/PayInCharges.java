package com.laitsneo.whitelbl.entity.Admin;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pay_in_charges")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PayInCharges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sl_no")
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull
    @Min(value = 1, message = "From range must be at least 1")
    @Column(name = "from_range", nullable = false)
    private Long fromRange;

    @NotNull
    @Min(value = 1, message = "To range must be at least 1")
    @Column(name = "to_range", nullable = false)
    private Long toRange;

    @NotBlank(message = "ChargesType is required")
    @Column(name = "charges_type", nullable = false, length = 50)
    private String chargesType;

    @NotNull(message = "Charges is required")
    @Positive(message = "Charges must be > 0")
    @Column(name = "charges_amount")  
    private Double chargesAmount;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

}