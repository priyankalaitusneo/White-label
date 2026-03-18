package com.laitsneo.whitelbl.repository.Admin;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.laitsneo.whitelbl.entity.Admin.SettlementRule;

public interface SettlementRuleRepository extends JpaRepository<SettlementRule, Long> {

	
	Optional<SettlementRule> findByUserId(String userId);
}
