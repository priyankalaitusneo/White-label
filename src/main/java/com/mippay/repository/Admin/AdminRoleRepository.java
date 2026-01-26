package com.mippay.repository.Admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Admin.AdminRole;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, String> {

    @Query(value = "select * from admin_role where admin_id =:adminId and role_id =:roleId", nativeQuery = true)
    Optional<AdminRole> findByAdminAndRoleId(String adminId, String roleId);

    @Query(value = "select role_id from admin_role where admin_id =:adminId", nativeQuery = true)
    List<String> findRoleByAdminId(String adminId);
    
    Optional<AdminRole> findByAdminIdAndRoleIdAndDeletedFalse(
            String adminId, String roleId);
    
    @Query(value = """
    	    SELECT 
    	        ar.admin_id     AS adminId,
    	        ar.user_name    AS userName,
    	        ar.email        AS email,
    	        ar.role_id      AS roleId,
    	        ar.permissions  AS permissions,
    	        ar.status       AS status,
    	        ar.deleted      AS deleted,
    	        ar.created_at   AS createdAt,
    	        ar.updated_at   AS updatedAt
    	    FROM admin_role ar
    	    ORDER BY ar.updated_at DESC
    	""", nativeQuery = true)
    	List<Map<String, Object>> findAllAdminRoles();



}
