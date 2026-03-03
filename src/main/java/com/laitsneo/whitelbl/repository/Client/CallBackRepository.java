package com.laitsneo.whitelbl.repository.Client;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.laitsneo.whitelbl.entity.Client.CallBack;

import java.util.Optional;

public interface CallBackRepository extends JpaRepository<CallBack, Integer> {

    @Query(value = "select * from call_back where transaction_id =:transactionId", nativeQuery = true)
    Optional<CallBack> findByTransactionId(String transactionId);

    @Transactional
    @Modifying
    @Query(value = "update call_back set status =:status where transaction_id =:transactionId", nativeQuery = true)
    void updateStatus(String status, String transactionId);
}
