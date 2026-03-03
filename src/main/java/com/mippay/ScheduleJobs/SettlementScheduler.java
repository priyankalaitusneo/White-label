//package com.mippay.ScheduleJobs;
//
////import com.mippay.service.SettlementService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//
// // Runs every hour to check for ready settlements
//@Component
//public class SettlementScheduler {
//
//    private static final Logger logger = LoggerFactory.getLogger(SettlementScheduler.class);
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
////    @Autowired
////    private SettlementService settlementService;
//
//    /**
//     * Process ready settlements every hour
//     * Cron expression: 0 0 * * * * (Every hour at minute 0)
//     *
//     * For testing, can change to every minute: 0 * * * * *
//     */
//    @Scheduled(cron = "0 0 * * * *") // Every hour
//    public void processSettlementsHourly() {
//        try {
//            LocalDateTime startTime = LocalDateTime.now();
//            logger.info("======================================");
//            logger.info("Settlement Scheduler Started at: {}", startTime.format(formatter));
//            logger.info("======================================");
//
//            // Process all ready settlements
//            settlementService.processReadySettlements();
//
//            LocalDateTime endTime = LocalDateTime.now();
//            long duration = java.time.Duration.between(startTime, endTime).getSeconds();
//
//            logger.info("======================================");
//            logger.info("Settlement Scheduler Completed at: {}", endTime.format(formatter));
//            logger.info("Total Duration: {} seconds", duration);
//            logger.info("======================================");
//
//        } catch (Exception e) {
//            logger.error("======================================");
//            logger.error("Settlement Scheduler Failed at: {}", LocalDateTime.now().format(formatter));
//            logger.error("Error: {}", e.getMessage(), e);
//            logger.error("======================================");
//        }
//    }
//
//    /**
//     * FOR TESTING ONLY: Process settlements every 5 minutes
//     * Uncomment this method and comment the hourly one for testing
//     */
//    // @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
//    public void processSettlementsForTesting() {
//        try {
//            logger.info("===== TESTING MODE: Processing Settlements (Every 5 min) =====");
//            settlementService.processReadySettlements();
//            logger.info("===== TESTING MODE: Completed =====");
//        } catch (Exception e) {
//            logger.error("===== TESTING MODE: Failed - {} =====", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Generate daily settlement report at 11:59 PM
//     * Cron expression: 0 59 23 * * * (Every day at 11:59 PM)
//     */
//    @Scheduled(cron = "0 59 23 * * *")
//    public void generateDailyReport() {
//        try {
//            logger.info("======================================");
//            logger.info("Generating Daily Settlement Report: {}", LocalDateTime.now().format(formatter));
//            logger.info("======================================");
//
//            // Get statistics and log summary
//            // You can enhance this to send email reports
//            logger.info("Daily settlement processing completed");
//            logger.info("======================================");
//
//        } catch (Exception e) {
//            logger.error("Failed to generate daily report: {}", e.getMessage(), e);
//        }
//    }
//
//    //* Health check log every 6 hours
//
//    @Scheduled(cron = "0 0 */6 * * *")
//    public void healthCheckLog() {
//        logger.info("Settlement Scheduler Health Check: System is running - {}",
//                   LocalDateTime.now().format(formatter));
//    }
//}