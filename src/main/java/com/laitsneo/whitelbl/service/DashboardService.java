package com.laitsneo.whitelbl.service;

import java.time.LocalDate;

import com.laitsneo.whitelbl.dto.Admin.DashboardRequestDTO;
import com.laitsneo.whitelbl.response.ClientDashboardResponseDTO;
import com.laitsneo.whitelbl.response.DashboardResponseDTO;

public interface DashboardService {

	DashboardResponseDTO getPayinDashboard(DashboardRequestDTO request);

	DashboardResponseDTO getPayoutDashboard(DashboardRequestDTO request);

	// =CLIENT DASHBOARD METHODS -
		
		ClientDashboardResponseDTO getClientPayinDashboard(String userId, LocalDate fromDate, LocalDate toDate);

		ClientDashboardResponseDTO getClientPayoutDashboard(String userId, LocalDate fromDate, LocalDate toDate);
	
}
