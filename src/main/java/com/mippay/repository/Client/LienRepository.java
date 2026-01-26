package com.mippay.repository.Client;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Client.LienAmount;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface LienRepository extends JpaRepository <LienAmount, Integer> {

    @Query(value = "select * from lien_amount where user_id =:userId", nativeQuery = true)
    Optional<LienAmount> findByUserId(String userId);

    @Transactional
    @Modifying
    @Query(value = "update lien_amount set amount =:lienAmount where user_id =:userId", nativeQuery = true)
    void updateAmount(String userId, Double lienAmount);

    @Query(value = "select w.*, c.name from lien_amount as w join clients as c where w.user_id = c.user_id", nativeQuery = true)
    List<Map<String,Object>> findAllLienAmountList();

    @Transactional
    @Modifying
    @Query(
        value = "DELETE FROM lien_amount WHERE user_id = :userId",
        nativeQuery = true
    )
    void deleteLien(@Param("userId") String userId);

    
    @Query(value = """
            SELECT 
                la.user_id,
                la.amount,
                la.description,
                la.created_date,
                c.name AS merchantName,
                (
                    SELECT status FROM lien_history 
                    WHERE user_id = la.user_id
                    ORDER BY timestamp DESC LIMIT 1
                ) AS latestStatus,
                (
                    SELECT timestamp FROM lien_history 
                    WHERE user_id = la.user_id
                    AND status = 'RELEASED'
                    ORDER BY timestamp DESC LIMIT 1
                ) AS releaseDate
            FROM lien_amount la
            JOIN clients c ON c.user_id = la.user_id
            WHERE (:userId IS NULL OR la.user_id = :userId)
            AND (:fromDate IS NULL OR DATE(la.created_date) >= :fromDate)
            AND (:toDate IS NULL OR DATE(la.created_date) <= :toDate)
        """, nativeQuery = true)
        List<Object[]> filterLienReports(
                @Param("userId") String userId,
                @Param("fromDate") LocalDate fromDate,
                @Param("toDate") LocalDate toDate
                
            
        );
        
        @Query(value = "SELECT COUNT(*) FROM clients WHERE user_id = :userId", nativeQuery = true)
        long countClientByUserId(@Param("userId") String userId);
        
        
        @Query(
        	    value = """
        	        SELECT 
        	            w.*,
        	            c.name AS clientName
        	        FROM lien_amount w
        	        JOIN clients c
        	            ON w.user_id = c.user_id
        	        WHERE w.user_id = :userId
        	        ORDER BY w.created_date DESC
        	        """,
        	    nativeQuery = true
        	)
        	List<Map<String, Object>> findLienAmountListByUserId(@Param("userId") String userId);



}
