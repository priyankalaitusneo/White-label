package com.laitsneo.mipPay.entity.Client;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class WebhookUrl {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@NotBlank(message = "userId should not be null..!")
	private String userId;
	@NotBlank(message = "url should not be null..!")
	private String url;

	private String webhooktype;
	@CreatedDate
	private Date createdDate = new Date();
	@UpdateTimestamp
	private Date updatedDate;

}
