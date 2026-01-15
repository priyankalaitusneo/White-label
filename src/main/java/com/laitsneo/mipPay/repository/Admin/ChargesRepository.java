package com.laitsneo.mipPay.repository.Admin;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.laitsneo.mipPay.entity.Admin.Charges;

import java.util.List;
import java.util.Map;

@Repository
public interface ChargesRepository extends JpaRepository<Charges,Long> {


    @Query(value = "select * from charges where sl_no =:slNo", nativeQuery = true)
    Charges fetchBySlNo(int slNo);

    @Query(value = "select * from charges where user_id =:userId and ((:fromRange between from_range and to_range) or (:toRange between from_range and to_range))",nativeQuery = true)
    List<Charges> fetchByClientIdAndRange(String userId, double fromRange, double toRange);

    @Modifying
    @Transactional
    @Query(value = "update charges set charges_type =:chargesType, charges =:charges where sl_no =:slNo", nativeQuery = true)
    void updateChargesBySlno(String chargesType, double charges, int slNo);

    @Modifying
    @Transactional
    @Query(value = "delete from charges where sl_no =:slNo", nativeQuery = true)
    void deleteBySlno(int slNo);

    @Query(value = "select * from charges where user_id =:userId", nativeQuery = true)
    List<Charges> fetchByUserId(String userId);

    @Query(value = "SELECT c.*,u.name FROM charges as c join clients as u where c.user_id = u.user_id", nativeQuery = true)
    List<Map<String, Object>> findAllCharges();
}
