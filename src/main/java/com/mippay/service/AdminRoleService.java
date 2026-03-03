package com.mippay.service;

import jakarta.validation.Valid;

import java.util.List;

import com.mippay.dto.Admin.AssignRoleRequestDto;
import com.mippay.response.AdminRoleResponseDto;

public interface AdminRoleService {

    List<String> getRoleByAdminId(String adminId);

    String assignRole(@Valid AssignRoleRequestDto request);

	String updateRole(String adminRoleId, AssignRoleRequestDto request);

	List<AdminRoleResponseDto> getAllRoles();

	String softDeleteRole(String adminRoleId);
}
