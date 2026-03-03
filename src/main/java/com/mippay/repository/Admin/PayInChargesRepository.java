package com.mippay.repository.Admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Admin.PayInCharges;

import java.util.List;
import java.util.Map;

@Repository
public interface PayInChargesRepository extends JpaRepository<PayInCharges, Long> {

    List<PayInCharges> findByUserId(String userId);

    boolean existsByUserIdAndFromRangeBetween(String userId, Long start, Long end);

    boolean existsByUserIdAndToRangeBetween(String userId, Long start, Long end);

    List<PayInCharges> findByUserIdOrderByFromRangeAsc(String userId);

    // Proper overlap check - detects overlapping ranges for same user
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM PayInCharges p " +
           "WHERE p.userId = :userId " +
           "AND p.fromRange <= :toRange " +
           "AND p.toRange >= :fromRange")
    boolean existsOverlap(@Param("userId") String userId,
                          @Param("fromRange") Long fromRange,
                          @Param("toRange") Long toRange);

    // Overlap check excluding current record during update
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM PayInCharges p " +
           "WHERE p.userId = :userId " +
           "AND p.id <> :excludeId " +
           "AND p.fromRange <= :toRange " +
           "AND p.toRange >= :fromRange")
    boolean existsOverlapExcluding(@Param("userId") String userId,
                                   @Param("fromRange") Long fromRange,
                                   @Param("toRange") Long toRange,
                                   @Param("excludeId") Long excludeId);

    // Fetch all PayInCharges with user details (JOIN with clients table)
    @Query(value = "SELECT p.*, c.name " +
                   "FROM pay_in_charges p " +
                   "JOIN clients c ON p.user_id = c.user_id",
           nativeQuery = true)
    List<Map<String, Object>> findAllPayInChargesWithUserDetails();

    // Find applicable charges for a specific amount
    @Query("""
            SELECT p FROM PayInCharges p
            WHERE p.userId = :userId
              AND :amount BETWEEN p.fromRange AND p.toRange
            ORDER BY p.fromRange ASC
            """)
    PayInCharges findApplicableCharges(@Param("userId") String userId,
                                       @Param("amount") double amount);

    // Fetch charges by userId and range
    @Query(value = "SELECT * FROM pay_in_charges WHERE user_id = :userId " +
                   "AND ((:amount BETWEEN from_range AND to_range) " +
                   "OR (:amount1 BETWEEN from_range AND to_range))",
           nativeQuery = true)
    List<PayInCharges> fetchByUserIdAndRange(String userId,
                                             Double amount,
                                             Double amount1);

    @Query(value = "select * from pay_in_charges where user_id =:userId and ((:fromRange between from_range and to_range) or (:toRange between from_range and to_range))",nativeQuery = true)
    List<PayInCharges> fetchByClientIdAndRange(@NotBlank(message = "userId is required") String userId, @NotNull(message = "fromRange is required") @PositiveOrZero(message = "fromRange must be >= 0") Long fromRange, @NotNull(message = "toRange is required") @Positive(message = "toRange must be > 0") Long toRange);
}
