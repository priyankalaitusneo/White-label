package com.laitsneo.mipPay.repository.Admin;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.laitsneo.mipPay.entity.Admin.Role;


@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    @Query(value = "select * from role where role_id in:arrayRole", nativeQuery = true)
    List<Role> getRolesByIds(String[] arrayRole);

//    @Query(value = "select role_name from role where role_id in:roleId", nativeQuery = true)
//    List<Role> findAllById(String[] roleId);

    @Query(value = "select role_name from role where role_id in(:roleIds)", nativeQuery = true)
    List<Map<String,Object>> findAllById(List<String> roleIds);
}
