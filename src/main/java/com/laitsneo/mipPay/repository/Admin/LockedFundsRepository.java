	package com.laitsneo.mipPay.repository.Admin;
	
	import com.laitsneo.mipPay.entity.Admin.LockedFunds;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
	import org.springframework.data.jpa.repository.Query;
	import org.springframework.data.repository.query.Param;
	import org.springframework.stereotype.Repository;
	
	import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
	import java.util.Optional;
	
	@Repository
	public interface LockedFundsRepository extends JpaRepository<LockedFunds, Long> {
	
	    // Find all locked funds by userId
	    List<LockedFunds> findByUserId(String userId);
	
	    // Find locked fund by id
	    Optional<LockedFunds> findById(Long id);
	
	    // Calculate total locked amount for a user
	    @Query("SELECT COALESCE(SUM(lf.amountLocked), 0) FROM LockedFunds lf WHERE lf.userId = :userId")
	    BigDecimal getTotalLockedAmountByUserId(@Param("userId") String userId);
	
	    // Get all locked funds with client details
	    @Query(value = """
	            SELECT 
	                lf.id AS lockId,
	                lf.user_id AS userId,
	                c.name AS merchantName,
	                lf.amount_locked AS amountLocked,
	                lf.reason AS reason,
	                lf.locked_date AS lockedDate,
	                lf.created_date AS createdDate,
	                lf.updated_date AS updatedDate,
	                c.account_bal AS currentBalance,
	                c.status AS merchantStatus
	            FROM locked_funds lf
	            JOIN clients c ON lf.user_id = c.user_id
	            ORDER BY lf.created_date DESC
	            """, nativeQuery = true)
	    List<Object[]> findAllLockedFundsWithClientDetails();
	
	    // Check if locked funds exist for a specific user
	    boolean existsByUserId(String userId);
	    
	    
	    @Query(
	            value = """
	                SELECT 
	                    lf.id                        AS lockId,
	                    CONCAT('LOCK', lf.id)        AS transactionId,
	                    lf.user_id                   AS userId,
	                    c.name                       AS merchantName,
	                    lf.amount_locked             AS amount,
	                    lf.reason                    AS reason,
	                    lf.locked_date               AS holdDate,
	                    CASE 
	                        WHEN lf.updated_date > lf.created_date THEN lf.updated_date
	                        ELSE NULL
	                    END                          AS releaseDate,
	                    CASE 
	                        WHEN lf.updated_date > lf.created_date THEN 'RELEASED'
	                        ELSE 'ON_HOLD'
	                    END                          AS status
	                FROM locked_funds lf
	                JOIN clients c ON lf.user_id = c.user_id
	                WHERE (:merchantName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :merchantName, '%')))
	                  AND (:status IS NULL OR 
	                        (:status = 'ON_HOLD' AND lf.updated_date = lf.created_date) OR
	                        (:status = 'RELEASED' AND lf.updated_date > lf.created_date)
	                      )
	                  AND (:fromDate IS NULL OR DATE(lf.locked_date) >= :fromDate)
	                  AND (:toDate IS NULL OR DATE(lf.locked_date) <= :toDate)
	                ORDER BY lf.locked_date DESC
	                """,
	            countQuery = """
	                SELECT COUNT(*)
	                FROM locked_funds lf
	                JOIN clients c ON lf.user_id = c.user_id
	                WHERE (:merchantName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :merchantName, '%')))
	                  AND (:status IS NULL OR 
	                        (:status = 'ON_HOLD' AND lf.updated_date = lf.created_date) OR
	                        (:status = 'RELEASED' AND lf.updated_date > lf.created_date)
	                      )
	                  AND (:fromDate IS NULL OR DATE(lf.locked_date) >= :fromDate)
	                  AND (:toDate IS NULL OR DATE(lf.locked_date) <= :toDate)
	                """,
	            nativeQuery = true
	        )
	        Page<Object[]> getLockedFundsReport(
	                @Param("merchantName") String merchantName,
	                @Param("status") String status,
	                @Param("fromDate") LocalDate fromDate,
	                @Param("toDate") LocalDate toDate,
	                Pageable pageable
	        );
	}