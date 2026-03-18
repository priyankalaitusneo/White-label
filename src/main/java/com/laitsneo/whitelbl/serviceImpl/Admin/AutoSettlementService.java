package com.laitsneo.whitelbl.serviceImpl.Admin;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.laitsneo.whitelbl.entity.Client.SettlementRecord;
import com.laitsneo.whitelbl.repository.Client.ClientRepository;
import com.laitsneo.whitelbl.repository.Client.PayinRecordRepository;
import com.laitsneo.whitelbl.repository.Client.SettlementRecordRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoSettlementService {
	
	
    private final PayinRecordRepository payinRepo;
    private final SettlementRecordRepository settlementRepo;
    private final ClientRepository clientRepo;
    
    @Scheduled(cron = "0 0 20 * * *") // daily at 8PM
    @Transactional
    public void runAutoSettlement() {

        log.info("START SETTLEMENT");

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime today8PM = now.toLocalDate().atTime(20, 0);
        LocalDateTime fromDT = today8PM.minusDays(1);
        LocalDateTime toDT = today8PM;

        String from = fromDT.toString().replace("T", " ");
        String to = toDT.toString().replace("T", " ");

        log.info("FROM {} TO {}", from, to);

        List<String> userIds = payinRepo.findDistinctUserIds(from, to);

        log.info("Users: {}", userIds);

        for (String userId : userIds) {

            Double amount = payinRepo.getTotalUnsettledAmount(userId, from, to);

            log.info("User {} amount {}", userId, amount);

            if (amount == null || amount <= 0) continue;

            SettlementRecord sr = new SettlementRecord();
            sr.setUserId(userId);
            sr.setSettlementAmount(amount);
            sr.setTotalUnsettledAmount(amount);
            sr.setFromDate(fromDT.toLocalDate());
            sr.setToDate(toDT.toLocalDate());
            sr.setSettlementMethod("BANK");
            sr.setStatus("SETTLED");
            sr.setSettlementStatus("COMPLETED");

            settlementRepo.save(sr);

            int updated = payinRepo.markSettled(userId, from, to);

            log.info("Updated rows {}", updated);
        }

        log.info("END SETTLEMENT");
    }
}
