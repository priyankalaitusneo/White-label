package com.laitsneo.mipPay.service;

import java.time.LocalDate;

import com.laitsneo.mipPay.dto.Admin.DashboardRequestDTO;
import com.laitsneo.mipPay.response.ClientDashboardResponseDTO;
import com.laitsneo.mipPay.response.DashboardResponseDTO;

public interface DashboardService {

	DashboardResponseDTO getPayinDashboard(DashboardRequestDTO request);

	DashboardResponseDTO getPayoutDashboard(DashboardRequestDTO request);

	// =CLIENT DASHBOARD METHODS -
		
		ClientDashboardResponseDTO getClientPayinDashboard(String userId, LocalDate fromDate, LocalDate toDate);

		ClientDashboardResponseDTO getClientPayoutDashboard(String userId, LocalDate fromDate, LocalDate toDate);
	
}
