package com.laitsneo.mipPay.repository.Admin;

import com.laitsneo.mipPay.entity.Admin.Switching;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SwitchingRepository extends JpaRepository<Switching, Long> {

    // Get all switching logs ordered by date and time (latest first)
    @Query("SELECT s FROM Switching s ORDER BY s.date DESC, s.time DESC")
    List<Switching> findAllOrderByDateTimeDesc();

    // Get PAYIN switching logs only
    @Query("SELECT s FROM Switching s WHERE s.merchantType = 'PAYIN' ORDER BY s.date DESC, s.time DESC")
    List<Switching> findPayinLogsOrderByDateTimeDesc();

    // Get PAYOUT switching logs only
    @Query("SELECT s FROM Switching s WHERE s.merchantType = 'PAYOUT' ORDER BY s.date DESC, s.time DESC")
    List<Switching> findPayoutLogsOrderByDateTimeDesc();

    // Get switching logs by merchant ID
    List<Switching> findByMerchantIdOrderByDateDescTimeDesc(String merchantId);

    // Get latest switching log for a merchant (to know current pipe)
    @Query("SELECT s FROM Switching s WHERE s.merchantId = :merchantId ORDER BY s.date DESC, s.time DESC LIMIT 1")
    Switching findLatestByMerchantId(@Param("merchantId") String merchantId);

    // Get latest switching log for a merchant by type
    @Query("SELECT s FROM Switching s WHERE s.merchantId = :merchantId AND s.merchantType = :merchantType ORDER BY s.date DESC, s.time DESC LIMIT 1")
    Switching findLatestByMerchantIdAndType(@Param("merchantId") String merchantId, 
                                           @Param("merchantType") String merchantType);

    @Query("""
            SELECT 
                s.switchedPipe,
                COUNT(s.id),
                SUM(CASE WHEN s.merchantType = 'PAYIN' THEN 1 ELSE 0 END),
                SUM(CASE WHEN s.merchantType = 'PAYOUT' THEN 1 ELSE 0 END)
            FROM Switching s
            WHERE (:pipeName IS NULL OR s.switchedPipe = :pipeName)
              AND (:fromDate IS NULL OR s.date >= :fromDate)
              AND (:toDate IS NULL OR s.date <= :toDate)
            GROUP BY s.switchedPipe
            """)
	Page<Object[]> getPipeSummary(String pipeName, LocalDate fromDate, LocalDate toDate, Pageable pageable);

}