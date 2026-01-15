package com.laitsneo.mipPay.repository.Client;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.laitsneo.mipPay.entity.Client.IpAddress;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface IpRepository extends JpaRepository<IpAddress, Integer> {

    @Query(value = "select * from ip_address where user_id =:userId", nativeQuery = true)
    Optional<IpAddress> findByUserId(String userId);


    @Transactional
    @Modifying
    @Query(value = "update ip_address set ip_address =:ipAddress where user_id =:userId", nativeQuery = true)
    void updateIp(String userId, String ipAddress);

//    @Query(value = "select w.*, c.name from ip_address as w join clients as c where w.user_id = c.user_id", nativeQuery = true)
//    List<Map<String,Object>> findAllIpList();
    @Query(
    	    value = """
    	        SELECT 
    	            w.*,
    	            c.name
    	        FROM ip_address w
    	        LEFT JOIN clients c 
    	            ON w.user_id = c.user_id
    	        ORDER BY w.created_date DESC
    	        """,
    	    nativeQuery = true
    	)
    	List<Map<String, Object>> findAllIpList();

}
