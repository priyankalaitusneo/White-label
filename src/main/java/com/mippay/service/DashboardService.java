package com.mippay.service;

import java.time.LocalDate;
import java.util.Map;

import com.mippay.dto.Admin.DashboardRequestDTO;
import com.mippay.response.ClientDashboardResponseDTO;
import com.mippay.response.DashboardResponseDTO;
import org.springframework.http.ResponseEntity;


public interface DashboardService {

	DashboardResponseDTO getPayinDashboard(DashboardRequestDTO request);

	DashboardResponseDTO getPayoutDashboard(DashboardRequestDTO request);

	// =CLIENT DASHBOARD METHODS -
		
		ClientDashboardResponseDTO getClientPayinDashboard(String userId, LocalDate fromDate, LocalDate toDate);

		ClientDashboardResponseDTO getClientPayoutDashboard(String userId, LocalDate fromDate, LocalDate toDate);

    ResponseEntity<?> payinDataByDate(Map<String, Object> data);
}
