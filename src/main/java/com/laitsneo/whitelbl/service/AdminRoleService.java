package com.laitsneo.whitelbl.service;

import jakarta.validation.Valid;

import java.util.List;

import com.laitsneo.whitelbl.dto.Admin.AssignRoleRequestDto;
import com.laitsneo.whitelbl.entity.Admin.AdminRole;
import com.laitsneo.whitelbl.response.AdminRoleResponseDto;

public interface AdminRoleService {

    List<String> getRoleByAdminId(String adminId);

    String assignRole(@Valid AssignRoleRequestDto request);

	String updateRole(String adminRoleId, AssignRoleRequestDto request);

	List<AdminRoleResponseDto> getAllRoles();

	String softDeleteRole(String adminRoleId);
}
