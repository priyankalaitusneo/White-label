package com.mippay.service;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import com.mippay.entity.Admin.Role;

public interface RoleService {

    List<Map<String,Object>> getRolesByRolesId(List<String> roleIds);

    String createRole(@Valid Role request);
}
