package com.laitsneo.whitelbl.serviceImpl.Admin;

import com.laitsneo.whitelbl.dto.Admin.AssignRoleRequestDto;
import com.laitsneo.whitelbl.entity.Admin.AdminRole;
import com.laitsneo.whitelbl.repository.Admin.AdminRoleRepository;
import com.laitsneo.whitelbl.response.AdminRoleResponseDto;
import com.laitsneo.whitelbl.service.AdminRoleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {
	
	
    Logger logger = LoggerFactory.getLogger(AdminRoleServiceImpl.class);

    @Autowired
    private AdminRoleRepository adminRoleRepository;


//    public String assignRole(AdminRole request) {
//    	logger.info("assignRole() → Request to assign roleId: {} to adminId: {}", 
//                 request.getRoleId(), request.getAdminId());
//        Optional<AdminRole> adminRole = this.adminRoleRepository
//                .findByAdminAndRoleId(request.getAdminId(), request.getRoleId());
//        if (adminRole.isEmpty()) {
//            AdminRole result = this.adminRoleRepository.save(request);
//            logger.info("assignRole() → Role assigned successfully for adminId: {}", 
//                     request.getAdminId());
//            return "Role assigned successfully..!";
//        } else {
//        	logger.warn("assignRole() → Same role already assigned for adminId: {}", 
//                     request.getAdminId());
//            return "Same role is already assigned for given Admin Id..!";
//        }
//    }

    @Override
    public String assignRole(AssignRoleRequestDto request) {

        Optional<AdminRole> existing =
            adminRoleRepository.findByAdminIdAndRoleIdAndDeletedFalse(
                request.getAdminId(), request.getRoleId());

        if (existing.isPresent()) {
            return "Role already assigned to this user";
        }

        AdminRole adminRole = AdminRole.builder()
                .adminId(request.getAdminId())
                .userName(request.getUserName())
                .email(request.getEmail())
                .roleId(request.getRoleId())
                .permissions(request.getPermissions())
                .status(
                    request.getStatus() == null ? "ACTIVE" : request.getStatus()
                )
                .deleted(false)
                .build();

        adminRoleRepository.save(adminRole);
        return "Role assigned successfully";
    }


    @Override
    public List<String> getRoleByAdminId(String adminId) {
    	logger.info("getRoleByAdminId() → Fetching roles for adminId: {}", adminId);
        List<String> roles = this.adminRoleRepository.findRoleByAdminId(adminId);
        logger.info("getRoleByAdminId() → {} roles found for adminId: {}", 
                 roles.size(), adminId);
        return roles;
    }


    @Override
    public String updateRole(String adminRoleId, AssignRoleRequestDto request) {

        AdminRole adminRole = adminRoleRepository.findById(adminRoleId)
                .orElseThrow(() -> new RuntimeException("Role assignment not found"));

        adminRole.setRoleId(request.getRoleId());
        adminRole.setPermissions(request.getPermissions());
        adminRole.setStatus(request.getStatus());

        adminRoleRepository.save(adminRole);
        return "Role updated successfully";
    }

    @Override
    public List<AdminRoleResponseDto> getAllRoles() {

        List<Map<String, Object>> rows =
                adminRoleRepository.findAllAdminRoles();

        List<AdminRoleResponseDto> list = new ArrayList<>();

        for (Map<String, Object> row : rows) {

            AdminRoleResponseDto dto = AdminRoleResponseDto.builder()
                    .adminId(String.valueOf(row.get("adminId")))
                    .userName(String.valueOf(row.get("userName")))
                    .email(String.valueOf(row.get("email")))
                    .roleName(String.valueOf(row.get("roleId"))) // TEMP: show roleId
                    .permissions(String.valueOf(row.get("permissions")))
                    .status(String.valueOf(row.get("status")))
                    .build();

            list.add(dto);
        }

        return list;
    }




    @Override
    public String softDeleteRole(String adminRoleId) {

        AdminRole adminRole = adminRoleRepository.findById(adminRoleId)
                .orElseThrow(() -> new RuntimeException("Role assignment not found"));

        adminRole.setDeleted(true);
        adminRole.setStatus("INACTIVE");

        adminRoleRepository.save(adminRole);
        return "Role removed successfully (soft delete)";
    }

}
