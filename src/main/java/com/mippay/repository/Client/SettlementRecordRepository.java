package com.mippay.repository.Client;

import com.mippay.entity.Client.SettlementRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface SettlementRecordRepository extends JpaRepository<SettlementRecord, Long> {

    
    //settlement by unique settlement ID
     
    Optional<SettlementRecord> findBySettlementId(String settlementId);

    // all settlements by merchant user ID
     
    List<SettlementRecord> findByUserId(String userId);

    // settlements by user ID and status
     
    List<SettlementRecord> findByUserIdAndStatus(String userId, String status);

    // settlement by UTR number (for bank transfers)
     
    Optional<SettlementRecord> findByUtrNumber(String utrNumber);

    // Check if UTR number exists
     
    boolean existsByUtrNumber(String utrNumber);

    // UNSETTLED AMOUNT QUERIES 

    // Get total unsettled amount for a merchant within date range
    
    @Query("""
        SELECT COALESCE(SUM(p.finalAmount), 0.0)
        FROM PayinRecords p
        WHERE p.userId = :userId
        AND p.status = 'SUCCESS'
        AND (p.settlementStatus = 'UNSETTLED' OR p.settlementStatus = 'PENDING' OR p.settlementStatus IS NULL)
        AND DATE(p.createdDate) >= :fromDate
        AND DATE(p.createdDate) <= :toDate
    """)
    Double calculateUnsettledAmount(
        @Param("userId") String userId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Get list of all merchants with unsettled funds
     * Groups by userId and returns merchant info with unsettled amounts
     */
    @Query("""
        SELECT p.userId, p.name, SUM(p.finalAmount) as unsettledAmount, COUNT(p.id) as transactionCount
        FROM PayinRecords p
        WHERE p.status = 'SUCCESS'
        AND (p.settlementStatus = 'UNSETTLED' OR p.settlementStatus = 'PENDING' OR p.settlementStatus IS NULL)
        GROUP BY p.userId, p.name
        HAVING SUM(p.finalAmount) > 0
        ORDER BY unsettledAmount DESC
    """)
    List<Object[]> findAllMerchantsWithUnsettledFunds();

    /**
     * Get unsettled merchants with date range filter
     */
    @Query("""
        SELECT p.userId, p.name, p.email, p.mobile, SUM(p.finalAmount) as unsettledAmount, 
               COUNT(p.id) as transactionCount, MAX(p.createdDate) as lastTransactionDate
        FROM PayinRecords p
        WHERE p.status = 'SUCCESS'
        AND (p.settlementStatus = 'UNSETTLED' OR p.settlementStatus = 'PENDING' OR p.settlementStatus IS NULL)
        AND (:fromDate IS NULL OR DATE(p.createdDate) >= :fromDate)
        AND (:toDate IS NULL OR DATE(p.createdDate) <= :toDate)
        GROUP BY p.userId, p.name, p.email, p.mobile
        HAVING SUM(p.finalAmount) > 0
        ORDER BY unsettledAmount DESC
    """)
    List<Object[]> findMerchantsWithUnsettledFundsByDateRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    // ========== OVERLAPPING CHECK ==========

    /**
     * Check if there are any overlapping settlements for same PayinRecords
     * Returns count of PayinRecords that are already in IN_PROGRESS or SETTLED status
     */
    @Query("""
        SELECT COUNT(p.id)
        FROM PayinRecords p
        WHERE p.userId = :userId
        AND p.status = 'SUCCESS'
        AND DATE(p.createdDate) >= :fromDate
        AND DATE(p.createdDate) <= :toDate
        AND p.settlementStatus IN ('IN_PROGRESS', 'SETTLED')
    """)
    Long checkOverlappingSettlements(
        @Param("userId") String userId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    // ========== SCHEDULER QUERIES ==========

    /**
     * Find all settlements ready for processing (T+1 reached)
     * Status = IN_PROGRESS and scheduledSettlementDate <= now
     */
    @Query("""
        SELECT s FROM SettlementRecord s
        WHERE s.status = 'IN_PROGRESS'
        AND s.scheduledSettlementDate <= :currentTime
        ORDER BY s.scheduledSettlementDate ASC
    """)
    List<SettlementRecord> findPendingSettlementsForProcessing(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find all IN_PROGRESS settlements
     */
    List<SettlementRecord> findByStatus(String status);

    // ========== HISTORY QUERIES ==========

    /**
     * Find settlement history with multiple filters
     */
    @Query("""
        SELECT s FROM SettlementRecord s
        WHERE (:userId IS NULL OR s.userId = :userId)
        AND (:settlementId IS NULL OR s.settlementId = :settlementId)
        AND (:status IS NULL OR s.status = :status)
        AND (:settlementStatus IS NULL OR s.settlementStatus = :settlementStatus)
        AND (:settlementMethod IS NULL OR s.settlementMethod = :settlementMethod)
        AND (:fromDate IS NULL OR s.fromDate >= :fromDate)
        AND (:toDate IS NULL OR s.toDate <= :toDate)
        AND (:minAmount IS NULL OR s.settlementAmount >= :minAmount)
        AND (:maxAmount IS NULL OR s.settlementAmount <= :maxAmount)
        ORDER BY s.createdDate DESC
    """)
    Page<SettlementRecord> findSettlementHistory(
        @Param("userId") String userId,
        @Param("settlementId") String settlementId,
        @Param("status") String status,
        @Param("settlementStatus") String settlementStatus,
        @Param("settlementMethod") String settlementMethod,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("minAmount") Double minAmount,
        @Param("maxAmount") Double maxAmount,
        Pageable pageable
    );

    /**
     * Find settlements by date range
     */
    @Query("""
        SELECT s FROM SettlementRecord s
        WHERE s.createdDate >= :fromDate
        AND s.createdDate <= :toDate
        ORDER BY s.createdDate DESC
    """)
    List<SettlementRecord> findByCreatedDateBetween(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    // ========== UPDATE QUERIES ==========

    /**
     * Update settlement status
     */
    @Modifying
    @Query("""
        UPDATE SettlementRecord s
        SET s.status = :status,
            s.updatedDate = :updatedDate
        WHERE s.settlementId = :settlementId
    """)
    int updateStatus(
        @Param("settlementId") String settlementId,
        @Param("status") String status,
        @Param("updatedDate") LocalDateTime updatedDate
    );

    /**
     * Complete settlement - update status and actual settlement date
     */
    @Modifying
    @Query("""
        UPDATE SettlementRecord s
        SET s.status = :status,
            s.settlementStatus = :settlementStatus,
            s.actualSettlementDate = :actualSettlementDate,
            s.updatedDate = :updatedDate
        WHERE s.settlementId = :settlementId
    """)
    int completeSettlement(
        @Param("settlementId") String settlementId,
        @Param("status") String status,
        @Param("settlementStatus") String settlementStatus,
        @Param("actualSettlementDate") LocalDateTime actualSettlementDate,
        @Param("updatedDate") LocalDateTime updatedDate
    );

    // ========== STATISTICS QUERIES ==========

    // Get total settled amount for a merchant
     
    @Query("""
        SELECT COALESCE(SUM(s.settlementAmount), 0.0)
        FROM SettlementRecord s
        WHERE s.userId = :userId
        AND s.status = 'SETTLED'
    """)
    Double getTotalSettledAmount(@Param("userId") String userId);

    /**
     * Get settlement count by status
     */
    @Query("SELECT s.status, COUNT(s) FROM SettlementRecord s GROUP BY s.status")
    List<Object[]> getSettlementCountByStatus();

    // Get settlement summary for dashboard
    @Query("""
        SELECT s.settlementMethod, COUNT(s), SUM(s.settlementAmount)
        FROM SettlementRecord s
        WHERE s.status = 'SETTLED'
        AND s.createdDate >= :fromDate
        GROUP BY s.settlementMethod
    """)
    List<Object[]> getSettlementSummary(@Param("fromDate") LocalDateTime fromDate);
    
    
    //Get Settlement Report with filters
    
    @Query("""
    		SELECT s.actualSettlementDate, s.userId, s.merchantName, s.settlementAmount,
    		s.settlementMethod, s.fromAccountHolder, s.fromAccountNumber, s.fromBankName, s.fromIfscCode,
    		s.toAccountHolder, s.toAccountNumber, s.toBankName, s.toIfscCode,
    		s.utrNumber, s.settlementStatus, s.failureReason
    		FROM SettlementRecord s
    		WHERE (:merchantId IS NULL OR s.userId = :merchantId)
    		AND (:status IS NULL OR s.settlementStatus = :status)
    		AND (:fromDate IS NULL OR s.actualSettlementDate >= :fromDate)
    		AND (:toDate IS NULL OR s.actualSettlementDate <= :toDate)
    		AND s.actualSettlementDate IS NOT NULL
    		ORDER BY s.actualSettlementDate DESC
    		""")
    		List<Object[]> getSettlementReport(
    		@Param("merchantId") String merchantId,
    		@Param("status") String status,
    		@Param("fromDate") LocalDateTime fromDate,
    		@Param("toDate") LocalDateTime toDate
    		);

   // Setllements Reports 
    		/**
    	     * Get Settlement Report with filters - NEW QUERY FOR REPORTS
    	     * Shows: Settlement ID, Merchant Name, Amount, Settlement Date, Bank Name, Status
    	     */
    	    @Query("""
    	        SELECT s.settlementId, s.merchantName, s.settlementAmount, s.actualSettlementDate,
    	               s.toBankName, s.settlementStatus, s.settlementMethod, s.utrNumber
    	        FROM SettlementRecord s
    	        WHERE (:merchantId IS NULL OR s.userId = :merchantId)
    	        AND (:status IS NULL OR s.settlementStatus = :status)
    	        AND (:pipe IS NULL OR LOWER(s.toBankName) LIKE LOWER(CONCAT('%', :pipe, '%')))
    	        AND (:fromDate IS NULL OR s.actualSettlementDate >= :fromDate)
    	        AND (:toDate IS NULL OR s.actualSettlementDate <= :toDate)
    	        AND s.actualSettlementDate IS NOT NULL
    	        ORDER BY s.actualSettlementDate DESC
    	    """)
    	    List<Object[]> getSettlementReportData(
    	        @Param("merchantId") String merchantId,
    	        @Param("status") String status,
    	        @Param("pipe") String pipe,
    	        @Param("fromDate") LocalDateTime fromDate,
    	        @Param("toDate") LocalDateTime toDate
    	    );
    		
}