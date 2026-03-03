package com.mippay.repository.Client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Client.LienHistory;

import java.util.List;

@Repository
public interface LienHistoryRepository extends JpaRepository<LienHistory, Integer> {

    @Query(value = "select * from lien_history where user_id =:clientId", nativeQuery = true)
    List<LienHistory> findByClientId(String clientId);
}
