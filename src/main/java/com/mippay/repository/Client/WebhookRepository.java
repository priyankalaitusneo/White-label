package com.mippay.repository.Client;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Client.WebhookUrl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface WebhookRepository extends JpaRepository<WebhookUrl, Integer> {

    @Query(value = "select * from webhook_url where user_id =:userId", nativeQuery = true)
    Optional<WebhookUrl> findByUserIdAndUrl(String userId);

    @Transactional
    @Modifying
    @Query(
        value = """
            UPDATE webhook_url
            SET url = :url,
                webhooktype = :webhooktype,
                updated_date = NOW()
            WHERE user_id = :userId
            """,
        nativeQuery = true
    )
    void updateURl(
            @Param("userId") String userId,
            @Param("url") String url,
            @Param("webhooktype") String webhooktype
    );

    
    @Query(value = "select * from webhook_url where user_id =:client and webhooktype =:payin", nativeQuery = true)
    WebhookUrl findByUserIdAndType(String client, String payin);



    @Query(value = "select w.*, c.name from webhook_url as w join clients as c where w.user_id = c.user_id", nativeQuery = true)
    List<Map<String,Object>> findAllWebhookList();
    
    @Query(
            value = """
                SELECT 
                    w.*,
                    c.name AS clientName
                FROM webhook_url w
                JOIN clients c
                    ON w.user_id = c.user_id
                WHERE w.webhooktype = 'payin'
                """,
            nativeQuery = true
        )
        List<Map<String, Object>> findAllPayinWebhookList();


        @Query(
            value = """
                SELECT 
                    w.*,
                    c.name AS clientName
                FROM webhook_url w
                JOIN clients c
                    ON w.user_id = c.user_id
                WHERE w.webhooktype = 'payout'
                """,
            nativeQuery = true
        )
        List<Map<String, Object>> findAllPayoutWebhookList();
}
