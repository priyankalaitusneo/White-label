package com.laitsneo.whitelbl.repository.Admin;

import com.laitsneo.whitelbl.entity.Admin.Vendors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VendorsRepository extends JpaRepository<Vendors, String> {

    Optional<Vendors> findByVendorName(String vendorName);

    Optional<Vendors> findByVendorNameAndIdNot(String vendorName, String id);

    @Modifying
    @Transactional
    @Query("UPDATE Vendors v SET v.status = :status WHERE v.id = :id")
    int updateVendorStatus(@Param("id") String id, @Param("status") String status);

    @Query("SELECT v.amount FROM Vendors v WHERE v.id = :vendorId")
    Double getVendorAmountLimit(@Param("vendorId") String vendorId);
}