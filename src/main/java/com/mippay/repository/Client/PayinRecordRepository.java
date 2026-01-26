package com.mippay.repository.Client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Client.PayinRecords;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.repository.query.Param;

@Repository
public interface PayinRecordRepository extends JpaRepository<PayinRecords, Integer> {

	@Query(value = "select * from payin_records where trxnid =:trxnid", nativeQuery = true)
	PayinRecords findByTrxnId(String trxnid);

	PayinRecords findByOrderId(String orderId);

//  Payment Link Queries 

	@Query("""
			    SELECT r FROM PayinRecords r
			    WHERE r.userId = :userId
			    AND (:fromDate IS NULL OR DATE(r.createdDate) >= :fromDate)
			    AND (:toDate IS NULL OR DATE(r.createdDate) <= :toDate)
			    AND (:utr IS NULL OR r.utr = :utr)
			    AND (:txnId IS NULL OR r.trxnid = :txnId)
			    ORDER BY r.createdDate DESC
			""")
	List<PayinRecords> searchHistory(@Param("userId") String userId, @Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate, @Param("utr") String utr, @Param("txnId") String txnId);

	PayinRecords findByTrxnidAndUserId(String trxnid, String userId);

	@Modifying
	@Query("UPDATE Client c SET c.walletBalance = :balance WHERE c.userId = :userId")
	int updateWalletBalance(@Param("balance") Double balance, @Param("userId") String userId);

	// ========== NEW: PayIn Reports Query ==========
	@Query("SELECT p FROM PayinRecords p WHERE p.createdDate BETWEEN :fromDate AND :toDate ORDER BY p.createdDate DESC")
	List<PayinRecords> findAllPayinRecordsBetweenDates(@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);

	@Modifying
	@Query("UPDATE PayinRecords p SET p.holdAmount = :holdAmount, p.holdReason = :holdReason, "
			+ "p.holdStatus = :holdStatus, p.finalAmount = :finalAmount, p.updatedDate = :updatedDate "
			+ "WHERE p.orderId = :orderId")
	int updateHoldAmount(@Param("orderId") String orderId, @Param("holdAmount") Double holdAmount,
			@Param("holdReason") String holdReason, @Param("holdStatus") String holdStatus,
			@Param("finalAmount") Double finalAmount, @Param("updatedDate") LocalDateTime updatedDate);

//	 @Query(value = "SELECT " +
//	            "pr.trxnid AS txnId, " +
//	            "pr.name AS customerName, " +
//	            "pr.status AS status, " +
//	            "pr.payment_method AS method, " +
//	            "pr.amount AS amount, " +
//	            "pr.created_date AS date " +
//	            "FROM payin_records pr " +
//	            "WHERE (:merchantId IS NULL OR pr.user_id = :merchantId) " +
//	            "AND (:status IS NULL OR pr.status = :status) " +
//	            "AND (:txnId IS NULL OR pr.trxnid = :txnId) " +
//	            "AND (:fromDate IS NULL OR DATE(pr.created_date) >= :fromDate) " +
//	            "AND (:toDate IS NULL OR DATE(pr.created_date) <= :toDate) " +
//	            "ORDER BY pr.created_date DESC",
//	            nativeQuery = true)
//	    List<Object[]> getPayinReport(
//	            @Param("merchantId") String merchantId,
//	            @Param("status") String status,
//	            @Param("txnId") String txnId,
//	            @Param("fromDate") LocalDate fromDate,
//	            @Param("toDate") LocalDate toDate
//	    );

	// Modified existing method with pagination
	@Query(
		    value = """
		        SELECT pr.*
		        FROM payin_records pr
		        WHERE (:merchantId IS NULL OR pr.user_id = :merchantId)
		          AND (:status IS NULL OR pr.status = :status)
		          AND (:txnId IS NULL OR pr.trxnid = :txnId)
		          AND (:fromDate IS NULL OR DATE(pr.created_date) >= :fromDate)
		          AND (:toDate IS NULL OR DATE(pr.created_date) <= :toDate)
		        ORDER BY pr.created_date DESC
		        LIMIT :pageSize OFFSET :offset
		        """,
		    nativeQuery = true
		)
		List<Map<String, Object>> getPayinReport(
		        @Param("merchantId") String merchantId,
		        @Param("status") String status,
		        @Param("txnId") String txnId,
		        @Param("fromDate") LocalDate fromDate,
		        @Param("toDate") LocalDate toDate,
		        @Param("offset") int offset,
		        @Param("pageSize") int pageSize
		);



	// New method for count
	@Query(value = "SELECT COUNT(*) " + "FROM payin_records pr "
			+ "WHERE (:merchantId IS NULL OR pr.user_id = :merchantId) "
			+ "AND (:status IS NULL OR pr.status = :status) " + "AND (:txnId IS NULL OR pr.trxnid = :txnId) "
			+ "AND (:fromDate IS NULL OR DATE(pr.created_date) >= :fromDate) "
			+ "AND (:toDate IS NULL OR DATE(pr.created_date) <= :toDate)", nativeQuery = true)
	Long getPayinReportCount(@Param("merchantId") String merchantId, @Param("status") String status,
			@Param("txnId") String txnId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query(value = """
			    SELECT * FROM payin_records
			    WHERE hold_amount > 0
			    AND (:merchantId IS NULL OR user_id = :merchantId)
			    AND (:status IS NULL OR hold_status = :status)
			    AND (:txnId IS NULL OR trxnid = :txnId)
			    AND (:pipe IS NULL OR pg_id = :pipe)
			    AND (:fromDate IS NULL OR DATE(created_date) >= :fromDate)
			    AND (:toDate IS NULL OR DATE(created_date) <= :toDate)
			    ORDER BY created_date DESC
			""", nativeQuery = true)
	List<PayinRecords> filterHoldReports(@Param("merchantId") String merchantId, @Param("status") String status,
			@Param("txnId") String txnId, @Param("pipe") String pipe, @Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
	
	@Query(
		    value = """
		        SELECT
		            SUM(CASE WHEN pr.status = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
		            SUM(CASE WHEN pr.status = 'FAILED' THEN 1 ELSE 0 END)  AS failedCount,
		            SUM(CASE WHEN pr.status = 'REJECTED' THEN 1 ELSE 0 END) AS rejectedCount
		        FROM payin_records pr
		        WHERE (:merchantId IS NULL OR pr.user_id = :merchantId)
		          AND (:txnId IS NULL OR pr.trxnid = :txnId)
		          AND (:fromDate IS NULL OR DATE(pr.created_date) >= :fromDate)
		          AND (:toDate IS NULL OR DATE(pr.created_date) <= :toDate)
		        """,
		    nativeQuery = true
		)
		Map<String, Object> getPayinSummaryCounts(
		        @Param("merchantId") String merchantId,
		        @Param("txnId") String txnId,
		        @Param("fromDate") LocalDate fromDate,
		        @Param("toDate") LocalDate toDate
		);

	

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

			FROM payin_records
			WHERE created_date >= :fromDate
			AND created_date <= :toDate
			AND (:vendorId IS NULL OR pg_id = :vendorId)
			""", nativeQuery = true)
	Map<String, Object> getPayinDashboardData(@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate, @Param("vendorId") String vendorId);

	@Query(value = """
			SELECT *
			FROM payin_records
			WHERE user_id = :clientId
			ORDER BY created_date DESC
			""", nativeQuery = true)
	List<PayinRecords> findByClientId(@Param("clientId") String clientId);
	
	@Query(
		    value = """
		        SELECT *
		        FROM payin_records
		        WHERE user_id = :userId
		          AND (:status IS NULL OR status = :status)
		          AND (:paymentMethod IS NULL OR payment_method = :paymentMethod)
		          AND (:fromDate IS NULL OR DATE(created_date) >= :fromDate)
		          AND (:toDate IS NULL OR DATE(created_date) <= :toDate)
		        ORDER BY created_date DESC
		        LIMIT :pageSize OFFSET :offset
		        """,
		    nativeQuery = true
		)
		List<PayinRecords> findAllPayinByUserId(
		        @Param("userId") String userId,
		        @Param("status") String status,
		        @Param("paymentMethod") String paymentMethod,
		        @Param("fromDate") LocalDate fromDate,
		        @Param("toDate") LocalDate toDate,
		        @Param("offset") int offset,
		        @Param("pageSize") int pageSize
		);


	// ========== CLIENT DASHBOARD QUERIES - ==========

	// CLIENT DASHBOARD: Get today's payin summary for specific client

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

			FROM payin_records
			WHERE user_id = :userId
			AND DATE(created_date) = CURDATE()
			""", nativeQuery = true)
	Map<String, Object> getClientPayinTodaySummary(@Param("userId") String userId);

	// CLIENT DASHBOARD: Get yearly payin overview with monthly breakdown

	@Query(value = """
			SELECT
			    MONTH(created_date) as month,
			    MONTHNAME(created_date) as monthName,

			    COUNT(CASE WHEN status = 'SUCCESS' THEN 1 END) as successCount,
			    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pendingCount,
			    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failedCount,
			    COUNT(*) as totalCount

			FROM payin_records
			WHERE user_id = :userId
			AND created_date >= :fromDate
			AND created_date <= :toDate
			GROUP BY MONTH(created_date), MONTHNAME(created_date)
			ORDER BY MONTH(created_date)
			""", nativeQuery = true)
	List<Map<String, Object>> getClientPayinYearlyOverview(@Param("userId") String userId,
			@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

	@Query(
		    value = """
		        SELECT *
		        FROM payin_records
		        WHERE user_id = :clientId
		        ORDER BY created_date DESC
		        """,
		    countQuery = """
		        SELECT COUNT(*)
		        FROM payin_records
		        WHERE user_id = :clientId
		        """,
		    nativeQuery = true
		)
	Page<PayinRecords> findByClientIdWithPagination(String clientId, Pageable pageable);

}