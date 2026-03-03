package com.mippay.repository.Client;


import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Client.PayoutRecords;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<PayoutRecords,Integer> {

    PayoutRecords findByOrderId(String trxnId);
    
    // Get all payout records between date range
    @Query("SELECT p FROM PayoutRecords p WHERE p.createdDate >= :fromDate AND p.createdDate <= :toDate ORDER BY p.createdDate DESC")
    List<PayoutRecords> findAllPayoutRecordsBetweenDates(
        @Param("fromDate") LocalDateTime fromDate, 
        @Param("toDate") LocalDateTime toDate
    );
    
    // Get payout records by userId and date range
    @Query("SELECT p FROM PayoutRecords p WHERE p.userId = :userId AND p.createdDate >= :fromDate AND p.createdDate <= :toDate ORDER BY p.createdDate DESC")
    List<PayoutRecords> findPayoutRecordsByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("fromDate") LocalDateTime fromDate, 
        @Param("toDate") LocalDateTime toDate
    );

    @Transactional
    @Modifying
    @Query(value = "update payout_records set transaction_id=:transactionId, time_stamp =:timeStamp where order_id =:trxnId", nativeQuery = true)
    void updateTransactionId(String transactionId,String timeStamp, String trxnId);

    @Query(value = "select * from payout_records where user_id =:clientId", nativeQuery = true)
    List<PayoutRecords> findByClientId(String clientId);

    @Transactional
    @Modifying
    @Query(value = "update payout_records set status =:status, refund_status =:refundStatus, status_code =:statusCode, utr =:utr where order_id =:orderId", nativeQuery = true)
    void updateStatus(String status, String statusCode, String refundStatus,String utr,String orderId);

    @Query(
    		  value = """
    		    SELECT COUNT(*) AS count, SUM(amount) AS amount, status
    		    FROM payout_records
    		    WHERE DATE(created_date) = CURDATE()
    		    GROUP BY status
    		  """,
    		  nativeQuery = true
    		)
    		List<Map<String, Object>> transactionCountAndAmountToday();


    @Query(value = "SELECT count(*)as count, sum(final_amount) as amount, status FROM payout_records where user_id =:clientId and date(created_date) >=:from and date(created_date) <=:from group by status",nativeQuery = true)
    List<Map<String, Object>> transactionCountAndAmounByClientIdt(String clientId, String from);

    @Query(value = "SELECT count(*)as count, sum(final_amount) as amount, status FROM payout_records where and date(created_date) >=:from and date(created_date) <=:from group by status",nativeQuery = true)
    List<Map<String, Object>> transactionCountAndAmoun(String from);

    @Query(value = "select * from payout_records where utr =:utr", nativeQuery = true)
    Optional<PayoutRecords> findByUtr(String utr);

    @Query(value = "select * from payout_records where transaction_id =:transactionId", nativeQuery = true)
    Optional<PayoutRecords> findByTransactionId(String transactionId);

    @Query(value = "select * from payout_records where user_id =:clientId and utr =:utr and date(created_date) >=:fromDate and date(created_date) <=:toDate and transaction_id =:transactionId and status =:status and transfer_mode =:mode", nativeQuery = true)
    List<PayoutRecords> filterByAll(String utr, String transactionId, String fromDate, String toDate, String clientId, String status, String mode);

    @Query(value = "select * from payout_records where status =:status and user_id =:clientId", nativeQuery = true)
    List<PayoutRecords> findByClientIdAndStatus(String clientId, String status);

    @Query(value = "select * from payout_records where status =:status", nativeQuery = true)
    List<PayoutRecords> findByStatus(String status);

    @Query(value = "select * from payout_records where transfer_mode =:paymentMode and user_id =:clientId", nativeQuery = true)
    List<PayoutRecords> findByClientIdAndMode(String clientId, String paymentMode);

    @Query(value = "select * from payout_records where transfer_mode =:paymentMode", nativeQuery = true)
    List<PayoutRecords> findByMode(String paymentMode);

    @Query(value = "select * from payout_records where user_id =:clientId and status =:status and date(created_date) >=:fromDate and date(created_date) <=:toDate", nativeQuery = true)
    List<PayoutRecords> findByClientIdStatusAndDate(String clientId, String status, String fromDate, String toDate);

    @Query(value = "select * from payout_records where status =:status and date(created_date) >=:fromDate and date(created_date) <=:toDate", nativeQuery = true)
    List<PayoutRecords> findByStatusAndDate(String status, String fromDate, String toDate);

    @Query(value = "select * from payout_records where user_id =:clientId and transfer_mode =:transferMode and date(created_date) >=:fromDate and date(created_date) <=:toDate", nativeQuery = true)
    List<PayoutRecords> findByClientIdModeAndDate(String clientId, String transferMode, String fromDate, String toDate);

    @Query(value = "select * from payout_records where transfer_mode =:transferMode and date(created_date) >=:fromDate and date(created_date) <=:toDate", nativeQuery = true)
    List<PayoutRecords> findByModeAndDate(String transferMode, String fromDate, String toDate);

    @Query(value = "select * from payout_records where user_id =:clientId and status =:status and transfer_mode =:transferMode", nativeQuery = true)
    List<PayoutRecords> findByClientIdStatusAndMode(String clientId, String status, String transferMode);

    @Query(value = "select * from payout_records where status =:status and transfer_mode =:transferMode", nativeQuery = true)
    List<PayoutRecords> findByStatusAndMode(String status, String transferMode);

    @Query(value = "select * from payout_records where user_id =:clientId and status =:status and date(created_date) >=:fromDate and date(created_date) <=:toDate and transfer_mode =:transferMode", nativeQuery = true)
    List<PayoutRecords> findByClientIdStatusModeAndDate(String clientId, String status, String fromDate, String toDate, String transferMode);

    @Query(value = "select * from payout_records where status =:status and date(created_date) >=:fromDate and date(created_date) <=:toDate and transfer_mode =:transferMode", nativeQuery = true)
    List<PayoutRecords> findByStatusModeAndDate(String status, String fromDate, String toDate, String transferMode);

    @Query(value = "select * from payout_records where user_id =:clientId and date(created_date) >=:fromDate and date(created_date) <=:toDate",nativeQuery = true)
    List<PayoutRecords> findByClientIdAndDate(String clientId, String fromDate, String toDate);

    @Query(value = "select * from payout_records where date(created_date) >=:fromDate and date(created_date) <=:toDate",nativeQuery = true)
    List<PayoutRecords> findByDate(String fromDate, String toDate);

//    @Query(value = "SELECT " +
//            "pr.order_id AS txnId, " +
//            "pr.name AS customerName, " +
//            "pr.status AS status, " +
//            "pr.transfer_mode AS method, " +
//            "pr.amount AS amount, " +
//            "pr.created_date AS date " +
//            "FROM payout_records pr " +
//            "WHERE (:merchantId IS NULL OR pr.user_id = :merchantId) " +
//            "AND (:status IS NULL OR pr.status = :status) " +
//            "AND (:txnId IS NULL OR pr.order_id = :txnId) " +
//            "AND (:fromDate IS NULL OR pr.created_date >= :fromDate) " +
//            "AND (:toDate IS NULL OR pr.created_date <= :toDate) " +
//            "ORDER BY pr.created_date DESC",
//            nativeQuery = true)
//    List<Object[]> getPayoutReport(
//            @Param("merchantId") String merchantId,
//            @Param("status") String status,
//            @Param("txnId") String txnId,
//            @Param("fromDate") LocalDate fromDate,
//            @Param("toDate") LocalDate toDate
//    );
//    
    

    @Query(value = """
        SELECT 
            COALESCE(SUM(CASE 
                WHEN UPPER(status) IN ('SUCCESS', 'COMPLETED') 
                THEN COALESCE(final_amount, amount) 
                ELSE 0 
            END), 0) as successAmount,
            
            COUNT(CASE 
                WHEN UPPER(status) IN ('SUCCESS', 'COMPLETED') 
                THEN 1 
            END) as successCount,
            
            COALESCE(SUM(CASE 
                WHEN UPPER(status) IN ('PENDING', 'PROCESSING', 'INITIATED') 
                THEN COALESCE(final_amount, amount) 
                ELSE 0 
            END), 0) as pendingAmount,
            
            COUNT(CASE 
                WHEN UPPER(status) IN ('PENDING', 'PROCESSING', 'INITIATED') 
                THEN 1 
            END) as pendingCount,
            
            COALESCE(SUM(CASE 
                WHEN UPPER(status) IN ('FAILED', 'REJECTED', 'CANCELLED') 
                THEN COALESCE(final_amount, amount) 
                ELSE 0 
            END), 0) as failedAmount,
            
            COUNT(CASE 
                WHEN UPPER(status) IN ('FAILED', 'REJECTED', 'CANCELLED') 
                THEN 1 
            END) as failedCount,
            
            COUNT(*) as totalCount
            
        FROM payout_records
        WHERE created_date >= :fromDate 
        AND created_date <= :toDate
        AND (:vendorId IS NULL OR pg_id = :vendorId)
        """, nativeQuery = true)
    Map<String, Object> getPayoutDashboardData(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("vendorId") String vendorId
    );

    @Query(
    	    value = """
    	        SELECT *
    	        FROM payout_records
    	        WHERE user_id = :userId
    	          AND (:status IS NULL OR status = :status)
    	          AND (:paymentMethod IS NULL OR transfer_mode = :paymentMethod)
    	          AND (:fromDate IS NULL OR DATE(created_date) >= :fromDate)
    	          AND (:toDate IS NULL OR DATE(created_date) <= :toDate)
    	        ORDER BY created_date DESC
    	        LIMIT :pageSize OFFSET :offset
    	        """,
    	    nativeQuery = true
    	)
    	List<PayoutRecords> findAllPayoutByUserId(
    	        @Param("userId") String userId,
    	        @Param("status") String status,
    	        @Param("paymentMethod") String paymentMethod,
    	        @Param("fromDate") LocalDate fromDate,
    	        @Param("toDate") LocalDate toDate,
    	        @Param("offset") int offset,
    	        @Param("pageSize") int pageSize
    	);


 // == CLIENT DASHBOARD QUERIES 

    // CLIENT DASHBOARD: Get today's payout summary for specific client
    
    @Query(value = """
        SELECT 
            COALESCE(SUM(amount), 0) as totalAmount,
            
            COALESCE(SUM(CASE 
                WHEN status = 'SUCCESS' 
                THEN amount 
                ELSE 0 
            END), 0) as successAmount,
            
            COUNT(CASE 
                WHEN status = 'SUCCESS' 
                THEN 1 
            END) as successCount,
            
            COALESCE(SUM(CASE 
                WHEN status = 'PENDING' 
                THEN amount 
                ELSE 0 
            END), 0) as pendingAmount,
            
            COUNT(CASE 
                WHEN status = 'PENDING' 
                THEN 1 
            END) as pendingCount,
            
            COALESCE(SUM(CASE 
                WHEN status = 'FAILED' 
                THEN amount 
                ELSE 0 
            END), 0) as failedAmount,
            
            COUNT(CASE 
                WHEN status = 'FAILED' 
                THEN 1 
            END) as failedCount,
            
            COUNT(*) as totalCount
            
        FROM payout_records
        WHERE user_id = :userId
        AND DATE(created_date) = CURDATE()
        """, nativeQuery = true)
    Map<String, Object> getClientPayoutTodaySummary(@Param("userId") String userId);

    //CLIENT DASHBOARD: Get yearly payout overview with monthly breakdown
    
    @Query(value = """
        SELECT 
            MONTH(created_date) as month,
            MONTHNAME(created_date) as monthName,
            
            COUNT(CASE WHEN status = 'SUCCESS' THEN 1 END) as successCount,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pendingCount,
            COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failedCount,
            COUNT(*) as totalCount
            
        FROM payout_records
        WHERE user_id = :userId
        AND created_date >= :fromDate
        AND created_date <= :toDate
        GROUP BY MONTH(created_date), MONTHNAME(created_date)
        ORDER BY MONTH(created_date)
        """, nativeQuery = true)
    List<Map<String, Object>> getClientPayoutYearlyOverview(
            @Param("userId") String userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
    

    @Query(
    	    value = """
    	        SELECT COUNT(*)
    	        FROM payout_records pr
    	        WHERE (:merchantId IS NULL OR pr.user_id = :merchantId)
    	          AND (:status IS NULL OR pr.status = :status)
    	          AND (:txnId IS NULL OR pr.transaction_id = :txnId)
    	          AND (:fromDate IS NULL OR DATE(pr.created_date) >= :fromDate)
    	          AND (:toDate IS NULL OR DATE(pr.created_date) <= :toDate)
    	        """,
    	    nativeQuery = true
    	)
    	long getPayoutReportCount(
    	        @Param("merchantId") String merchantId,
    	        @Param("status") String status,
    	        @Param("txnId") String txnId,
    	        @Param("fromDate") LocalDate fromDate,
    	        @Param("toDate") LocalDate toDate
    	);


    @Query(
    	    value = """
    	        SELECT pr.*
    	        FROM payout_records pr
    	        WHERE (:merchantId IS NULL OR pr.user_id = :merchantId)
    	          AND (:status IS NULL OR pr.status = :status)
    	          AND (:txnId IS NULL OR pr.transaction_id = :txnId)
    	          AND (:fromDate IS NULL OR DATE(pr.created_date) >= :fromDate)
    	          AND (:toDate IS NULL OR DATE(pr.created_date) <= :toDate)
    	        ORDER BY pr.created_date DESC
    	        LIMIT :pageSize OFFSET :offset
    	        """,
    	    nativeQuery = true
    	)
    	List<Map<String, Object>> getPayoutReport(
    	        @Param("merchantId") String merchantId,
    	        @Param("status") String status,
    	        @Param("txnId") String txnId,
    	        @Param("fromDate") LocalDate fromDate,
    	        @Param("toDate") LocalDate toDate,
    	        @Param("offset") int offset,
    	        @Param("pageSize") int pageSize
    	);

    @Query(
    	    value = """
    	        SELECT
    	            SUM(CASE WHEN pr.status = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
    	            SUM(CASE WHEN pr.status = 'FAILED' THEN 1 ELSE 0 END)  AS failedCount,
    	            SUM(CASE WHEN pr.status = 'PENDING' THEN 1 ELSE 0 END) AS pendingCount
    	        FROM payout_records pr
    	        WHERE (:merchantId IS NULL OR pr.user_id = :merchantId)
    	          AND (:txnId IS NULL OR pr.transaction_id = :txnId)
    	          AND (:fromDate IS NULL OR DATE(pr.created_date) >= :fromDate)
    	          AND (:toDate IS NULL OR DATE(pr.created_date) <= :toDate)
    	        """,
    	    nativeQuery = true
    	)
    	Map<String, Object> getPayoutSummaryCounts(
    	        @Param("merchantId") String merchantId,
    	        @Param("txnId") String txnId,
    	        @Param("fromDate") LocalDate fromDate,
    	        @Param("toDate") LocalDate toDate
    	);

    @Query(
    	    value = "select * from payout_records where user_id = :clientId",
    	    countQuery = "select count(*) from payout_records where user_id = :clientId",
    	    nativeQuery = true
    	)
	Page<PayoutRecords> findByClientIForClient(String clientId, Pageable pageable);


 // For vendors amount validations

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM PayoutRecords p WHERE p.pgId = :vendorId AND p.status = 'SUCCESS'")
    Double getTotalSuccessAmountByVendor(@Param("vendorId") String vendorId);
    
    @Query(
    	    value = """
    	        SELECT 
    	            YEAR(created_date)  AS year,
    	            MONTH(created_date) AS month,
    	            status              AS status,
    	            COUNT(*)            AS count,
    	            SUM(amount)         AS amount
    	        FROM payout_records
    	        GROUP BY YEAR(created_date), MONTH(created_date), status
    	        ORDER BY year DESC, month DESC
    	        """,
    	    nativeQuery = true
    	)
    	List<Map<String, Object>> transactionCountAndAmountYearMonthWise();
    
    @Query(
    	    value = """
    	        SELECT
    	            YEAR(created_date)  AS year,
    	            MONTH(created_date) AS month,
    	            status              AS status,
    	            COUNT(*)            AS count,
    	            SUM(final_amount)   AS amount
    	        FROM payout_records
    	        WHERE user_id = :clientId
    	        GROUP BY YEAR(created_date), MONTH(created_date), status
    	        ORDER BY year DESC, month DESC
    	        """,
    	    nativeQuery = true
    	)
    	List<Map<String, Object>> transactionCountAndAmountClientYearMonth(
    	        @Param("clientId") String clientId
    	);
    @Query(
    	    value = """
    	        SELECT 
    	            COUNT(*)            AS count,
    	            SUM(final_amount)   AS amount,
    	            status              AS status
    	        FROM payout_records
    	        WHERE user_id = :clientId
    	        GROUP BY status
    	        """,
    	    nativeQuery = true
    	)
    	List<Map<String, Object>> transactionCountAndAmountOverallByClient(
    	        @Param("clientId") String clientId
    	);
    @Query(
    	    value = """
    	        SELECT
    	            COUNT(*)        AS count,
    	            SUM(amount)     AS amount,
    	            status
    	        FROM payout_records
    	        GROUP BY status
    	        """,
    	    nativeQuery = true
    	)
    	List<Map<String, Object>> transactionCountAndAmountOverall();


}