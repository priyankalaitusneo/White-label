package com.mippay.repository.Client;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mippay.entity.Client.Authentication;

import java.util.Optional;

@Repository
public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {

    Optional<Authentication> findByUserId(String userId);

    // Check if clientId already exists (to avoid duplicates if needed)
    boolean existsByClientId(String clientId);

    @Query(value = "select * from authentication where client_id =:clientId and client_secret =:clientSecret and user_id =:userId", nativeQuery = true)
    Authentication findByCredientials(String clientId, String clientSecret, String userId);

    @Query(value = "select pg_id from authentication where user_id =:userId", nativeQuery = true)
    String getPgIdByClientId(String userId);

}
