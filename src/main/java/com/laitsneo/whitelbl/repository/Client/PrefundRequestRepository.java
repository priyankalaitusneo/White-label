package com.laitsneo.whitelbl.repository.Client;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.laitsneo.whitelbl.entity.Client.PrefundRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface PrefundRequestRepository extends JpaRepository<PrefundRequest, Long> {

    boolean existsByReference(String reference);

    Optional<PrefundRequest> findByReferenceAndUserId(String reference, String userId);

    Optional<PrefundRequest> findByReference(String reference);

    @Query("SELECT p FROM PrefundRequest p WHERE p.userId = :userId AND p.status = 'PENDING'")
    java.util.List<PrefundRequest> findPendingRequestsByUserId(@Param("userId") String userId);

    @Query(value = "SELECT p.amount,p.reference,p.status,p.requested_date,p.approved_date,p.user_id,c.name,p.lien_status FROM prefundrequests as p join clients as c where p.user_id = c.user_id and p.user_id =:clientId", nativeQuery = true)
    List<Map<String,Object>> findByClientId(String clientId);

    @Query(value = "select p.amount,p.reference,p.status,p.requested_date,p.approved_date,p.user_id,c.name,p.lien_status from prefundrequests as p join clients as c where p.user_id = c.user_id and p.requested_date >=:fromDate and p.requested_date <:toDate ", nativeQuery = true)
    List<Map<String,Object>> prefundFilterByDate(String fromDate, String toDate);

    @Query(value = "select p.amount,p.reference,p.status,p.requested_date,p.approved_date,p.user_id,c.name,p.lien_status from prefundrequests as p join clients as c where p.user_id = c.user_id and p.requested_date >=:fromDate and p.requested_date <:toDate and p.user_id =:clientId", nativeQuery = true)
    List<Map<String,Object>> prefundFilterByDateAndClientId(String fromDate, String toDate, String clientId);

    @Transactional
    @Modifying
    @Query(value = "update prefundrequests set lien_status =:lienStatus where reference =:reference", nativeQuery = true)
    void updateLienStatus(String lienStatus, String reference);

    @Query(value = """
    	    SELECT 
    	        p.id,
    	        p.user_id           AS userId,
    	        p.reference,
    	        p.amount,
    	        p.status,
    	        p.requested_date    AS requestedDate,
    	        p.approved_date     AS approvedDate,
    	        p.admin_acc_num     AS adminAccNum,
    	        p.admin_ifsc        AS adminIfsc,
    	        p.client_acc_num    AS clientAccNum,
    	        c.ifsc_code         AS clientIfsc,
    	        p.paymentmethod     AS paymentMethod,
    	        p.approveby,
    	        p.remarks,
    	        c.name              AS clientName,
    	        c.bank_name         AS bankName
    	    FROM prefundrequests p
    	    JOIN clients c 
    	        ON p.user_id = c.user_id
    	    WHERE p.status = 'PENDING'
    	    """,
    	    countQuery = """
    	    SELECT COUNT(*)
    	    FROM prefundrequests p
    	    JOIN clients c 
    	        ON p.user_id = c.user_id
    	    WHERE p.status = 'PENDING'
    	    """,
    	    nativeQuery = true)
    	Page<Map<String, Object>> findAllList(Pageable pageable);




    
    
    @Query(
    	    value = """
    	        SELECT 
    	            p.*,

    	            c.name          AS clientName,
    	            c.email         AS email,
    	            c.mobile_num    AS mobileNum,
    	            c.ifsc_code     AS clientIfsc,
    	            c.account_num   AS clientAccountNum,
    	            c.bank_name     AS bankName,
    	            c.pan           AS pan,
    	            c.gst           AS gst,
    	            c.cin           AS cin,
    	            c.address       AS address,
    	            c.merchant_type AS merchantType,
    	            c.wallet_balance AS walletBalance,
    	            c.virtual_acc_no AS virtualAccNo,
    	            c.status        AS clientStatus

    	        FROM prefundrequests p
    	        LEFT JOIN clients c
    	            ON p.user_id = c.user_id
    	        WHERE p.status = :status
    	        ORDER BY p.approved_date DESC
    	        """,
    	    countQuery = """
    	        SELECT COUNT(*)
    	        FROM prefundrequests p
    	        WHERE p.status = :status
    	        """,
    	    nativeQuery = true
    	)
    	Page<Map<String, Object>> findByStatus(
    	        @Param("status") String status,
    	        Pageable pageable
    	);



    @Query(
    	    value = """
    	        SELECT 
    	            p.id,
    	            p.user_id           AS merchantId,
    	            p.reference,
    	            p.amount,
    	            p.status,
    	            p.requested_date    AS requestedDate,
    	            p.approved_date     AS approvedDate,
    	            p.approveby         AS approvedBy,
    	            p.admin_acc_num     AS fromAccount,
    	            p.client_acc_num    AS toAccount,
    	            p.paymentmethod     AS method,
    	            p.remarks,
    	            p.name              AS merchantName
    	        FROM prefundrequests p
    	        WHERE
    	            (:merchantId IS NULL OR p.user_id = :merchantId)
    	            AND (:status IS NULL OR :status = 'ALL' OR p.status = :status)
    	            AND (:fromDate IS NULL OR DATE(p.requested_date) >= :fromDate)
    	            AND (:toDate IS NULL OR DATE(p.requested_date) <= :toDate)
    	        ORDER BY p.requested_date DESC
    	        """,
    	    countQuery = """
    	        SELECT COUNT(*)
    	        FROM prefundrequests p
    	        WHERE
    	            (:merchantId IS NULL OR p.user_id = :merchantId)
    	            AND (:status IS NULL OR :status = 'ALL' OR p.status = :status)
    	            AND (:fromDate IS NULL OR DATE(p.requested_date) >= :fromDate)
    	            AND (:toDate IS NULL OR DATE(p.requested_date) <= :toDate)
    	        """,
    	    nativeQuery = true
    	)
    	Page<Map<String, Object>> getPrefundReports(
    	        @Param("merchantId") String merchantId,
    	        @Param("status") String status,
    	        @Param("fromDate") LocalDate fromDate,
    	        @Param("toDate") LocalDate toDate,
    	        Pageable pageable
    	);


    @Modifying
    @Query(value = """
        UPDATE prefundrequests
        SET lien_status = :lienStatus
        WHERE user_id = :userId
        """, nativeQuery = true)
    int updateLienStatusByUserId(
            @Param("userId") String userId,
            @Param("lienStatus") String lienStatus
    );
    
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE prefundrequests 
        SET lien_status = :lienStatus 
        WHERE user_id = :userId 
        AND status = 'APPROVED'
        """, nativeQuery = true)
    int updateLienStatusForApprovedRequests(
        @Param("userId") String userId, 
        @Param("lienStatus") String lienStatus);
    
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE prefundrequests 
        SET lien_status = :lienStatus 
        WHERE user_id = :userId 
        AND lien_status = 'LIEN_APPLIED'
        """, nativeQuery = true)
    int updateLienStatusForAppliedRequests(
        @Param("userId") String userId, 
        @Param("lienStatus") String lienStatus
    );

    @Query(value = """
    	    SELECT COALESCE(SUM(amount), 0)
    	    FROM prefundrequests
    	    WHERE user_id = :userId
    	      AND status = 'APPROVED'
    	    """, nativeQuery = true)
    	BigDecimal getTotalApprovedAmount(@Param("userId") String userId);

    @Query(value = """
    	    SELECT 
    	        p.*,
    	        p.name           AS clientName,
    	        c.email          AS email,
    	        c.mobile_num     AS mobileNum,
    	        c.ifsc_code      AS clientIfsc,
    	        c.account_num    AS clientAccountNum,
    	        c.bank_name      AS bankName,
    	        c.pan            AS pan,
    	        c.gst            AS gst,
    	        c.cin            AS cin,
    	        c.address        AS address,
    	        c.merchant_type  AS merchantType,
    	        c.wallet_balance AS walletBalance,
    	        c.virtual_acc_no AS virtualAccNo,
    	        c.status         AS clientStatus
    	    FROM prefundrequests p
    	    LEFT JOIN clients c
    	        ON p.user_id = c.user_id
    	    WHERE p.user_id = :userId
    	      AND UPPER(p.status) = 'PENDING'
    	    ORDER BY p.requested_date DESC
    	""", nativeQuery = true)
    	List<Map<String, Object>> findPendingListByUserId(
    	        @Param("userId") String userId
    	);




    @Query(
    	    value = """
    	        SELECT 
    	            p.*,
    	            p.name           AS clientName,
    	            c.email          AS email,
    	            c.mobile_num     AS mobileNum,
    	            c.ifsc_code      AS clientIfsc,
    	            c.account_num    AS clientAccountNum,
    	            c.bank_name      AS bankName,
    	            c.pan            AS pan,
    	            c.gst            AS gst,
    	            c.cin            AS cin,
    	            c.address        AS address,
    	            c.merchant_type  AS merchantType,
    	            c.wallet_balance AS walletBalance,
    	            c.virtual_acc_no AS virtualAccNo,
    	            c.status         AS clientStatus
    	        FROM prefundrequests p
    	        LEFT JOIN clients c
    	            ON p.user_id = c.user_id
    	        WHERE UPPER(p.status) = UPPER(:status)
    	          AND p.user_id = :userId
    	        ORDER BY p.approved_date DESC
    	        LIMIT :pageSize OFFSET :offset
    	        """,
    	    nativeQuery = true
    	)
    	List<Map<String, Object>> findByStatusAndUserId(
    	        @Param("status") String status,
    	        @Param("userId") String userId,
    	        @Param("offset") int offset,
    	        @Param("pageSize") int pageSize
    	);

    
    @Query(
    	    value = """
    	        SELECT 
    	            p.*,
    	            p.name           AS clientName,
    	            c.email          AS email,
    	            c.mobile_num     AS mobileNum,
    	            c.ifsc_code      AS clientIfsc,
    	            c.account_num    AS clientAccountNum,
    	            c.bank_name      AS bankName,
    	            c.pan            AS pan,
    	            c.gst            AS gst,
    	            c.cin            AS cin,
    	            c.address        AS address,
    	            c.merchant_type  AS merchantType,
    	            c.wallet_balance AS walletBalance,
    	            c.virtual_acc_no AS virtualAccNo,
    	            c.status         AS clientStatus
    	        FROM prefundrequests p
    	        LEFT JOIN clients c
    	            ON p.user_id = c.user_id
    	        WHERE p.user_id = :userId
    	        ORDER BY p.requested_date DESC
    	        LIMIT :pageSize OFFSET :offset
    	        """,
    	    nativeQuery = true
    	)
    	List<Map<String, Object>> findAllByUserId(
    	        @Param("userId") String userId,
    	        @Param("offset") int offset,
    	        @Param("pageSize") int pageSize
    	);

    
 

    @Query(
    	    value = """
    	        SELECT 
    	            p.amount,
    	            p.reference,
    	            p.status,
    	            p.requested_date,
    	            p.approved_date,
    	            p.user_id,
    	            c.name,
    	            p.lien_status
    	        FROM prefundrequests p
    	        JOIN clients c ON p.user_id = c.user_id
    	        WHERE p.user_id = :clientId
    	        """,
    	    countQuery = """
    	        SELECT COUNT(*)
    	        FROM prefundrequests p
    	        WHERE p.user_id = :clientId
    	        """,
    	    nativeQuery = true
    	)
	Page<Map<String, Object>> findByClientIdWithPagination(String clientId, Pageable pageable);
    
    
    @Query(
    	    value = """
    	        SELECT 
    	            p.id,
    	            p.user_id           AS userId,
    	            p.reference,
    	            p.amount,
    	            p.status,
    	            p.requested_date    AS requestedDate,
    	            p.approved_date     AS approvedDate,
    	            p.admin_acc_num     AS adminAccNum,
    	            p.admin_ifsc        AS adminIfsc,
    	            p.client_acc_num    AS clientAccNum,
    	            c.ifsc_code         AS clientIfsc,
    	            p.paymentmethod     AS paymentMethod,
    	            p.approveby,
    	            p.remarks,
    	            c.name              AS clientName,
    	            c.bank_name         AS bankName
    	        FROM prefundrequests p
    	        JOIN clients c ON p.user_id = c.user_id
    	        WHERE p.user_id = :userId
    	          AND UPPER(p.status) = 'PENDING'
    	        ORDER BY p.requested_date DESC
    	        """,
    	    countQuery = """
    	        SELECT COUNT(*)
    	        FROM prefundrequests p
    	        WHERE p.user_id = :userId
    	          AND UPPER(p.status) = 'PENDING'
    	        """,
    	    nativeQuery = true
    	)
    	Page<Map<String, Object>> findPendingPrefundByUserId(
    	        @Param("userId") String userId,
    	        Pageable pageable
    	);


    

}
