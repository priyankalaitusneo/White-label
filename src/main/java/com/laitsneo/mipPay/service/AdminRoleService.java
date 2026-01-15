package com.laitsneo.mipPay.service;

import jakarta.validation.Valid;

import java.util.List;

import com.laitsneo.mipPay.dto.Admin.AssignRoleRequestDto;
import com.laitsneo.mipPay.response.AdminRoleResponseDto;

public interface AdminRoleService {

    List<String> getRoleByAdminId(String adminId);

    String assignRole(@Valid AssignRoleRequestDto request);

	String updateRole(String adminRoleId, AssignRoleRequestDto request);

	List<AdminRoleResponseDto> getAllRoles();

	String softDeleteRole(String adminRoleId);
}
