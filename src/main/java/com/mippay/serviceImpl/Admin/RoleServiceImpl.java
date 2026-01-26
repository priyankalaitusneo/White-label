package com.mippay.serviceImpl.Admin;

import com.mippay.entity.Admin.Role;
import com.mippay.exception.CustomInternalServerErrorException;
import com.mippay.repository.Admin.RoleRepository;
import com.mippay.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {
	

    @Autowired
    RoleRepository rolesRepository;

    Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    public String createRole(Role role) {
        logger.info("Inside createRole() !!");
        try {
            Role result = this.rolesRepository.save(role);
            logger.info("Role created successfully !!");
            return "Role created successfully with role id: " + result.getRoleId();
        } catch (Exception e) {
            logger.error("Exception in createRole() : {}", e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                return (role.getRoleName() + " is already present !!");
            }
            throw new CustomInternalServerErrorException();
        }
    }


    public List<Map<String, Object>> getRolesByRolesId(List<String> roleIds) {
    	logger.info("getRolesByRolesId() → Request received for roleIds: {}", roleIds);
        List<Map<String, Object>> roles = this.rolesRepository.findAllById(roleIds);
        logger.info("getRolesByRolesId() → {} roles found for given roleIds", roles.size());
        return roles;
    }


    public List<Role> getAllRoles() {
    	logger.info("getAllRoles() → Fetching all roles");
        List<Role> roles = this.rolesRepository.findAll();
        logger.info("getAllRoles() → {} roles retrieved", roles.size());
        return roles;
    }

}
