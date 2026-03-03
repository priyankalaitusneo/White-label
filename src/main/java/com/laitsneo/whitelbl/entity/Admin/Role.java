package com.laitsneo.whitelbl.entity.Admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

	
	 @Id
	    @GeneratedValue(strategy = GenerationType.UUID)
	    private String roleId;

	    @Column(unique = true, nullable = false)
	    private String roleName;

	    @CreatedDate
	    private Date createDate= new Date();

		@UpdateTimestamp
		private Date updatedAt = new Date();
}
