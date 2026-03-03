package com.laitsneo.whitelbl.repository.Admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.laitsneo.whitelbl.entity.Admin.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = "select * from users where admin_id =:userId", nativeQuery = true)
    Optional<User> findByUserId(String userId);

    @Query(value = "select user_id,name,account_bal from clients", nativeQuery = true)
    List<Map<String, Object>> walletsListWithName();
}
