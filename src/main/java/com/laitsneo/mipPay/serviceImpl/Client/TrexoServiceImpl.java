package com.laitsneo.mipPay.serviceImpl.Client;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.laitsneo.mipPay.entity.Client.PayoutRecords;
import com.laitsneo.mipPay.repository.Client.PayoutRepository;
import com.laitsneo.mipPay.response.TrexoCheckStatusResponse;
import com.laitsneo.mipPay.service.TrexoService;

@Service
public class TrexoServiceImpl implements TrexoService {
	
    Logger logger = LoggerFactory.getLogger(TrexoServiceImpl.class);


    private final RestTemplate restTemplate = new RestTemplate();
    private final PayoutRepository payoutRepository;

    @Value("${trexo.key}")
    private String trexoKey;

    @Value("${trexo.secret}")
    private String trexoSecret;

    public TrexoServiceImpl(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Override
    public Map<String, Object> checkTransaction(String orderId) {
    	logger.info("checkTransaction() → Checking transaction for orderId: {}", orderId);
        String url = "https://customer.api.payout.trexoedge.com/api/v2/check_status/" + orderId;
        logger.info("checkTransaction() → Calling Trexo API: {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("x-trexo-key", trexoKey);
        headers.set("x-trexo-secret", trexoSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<TrexoCheckStatusResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, TrexoCheckStatusResponse.class);
        logger.info("checkTransaction() → Trexo API response received for orderId: {}", orderId);
        TrexoCheckStatusResponse trexoRes = response.getBody();
        if (trexoRes != null) {
        	logger.info("checkTransaction() → Valid response received, updating order status");
            return updateOrderStatus(orderId, trexoRes);
        }
        logger.warn("checkTransaction() → No response body for orderId: {}", orderId);
        Map<String,Object> map = new HashMap<>();
        map.put("status", "Error");
        map.put("message", "BAD_REQUEST");
        map.put("data", "Order Id not found..!");
        return map;
    }


    private Map<String,Object> updateOrderStatus(String orderId, TrexoCheckStatusResponse trexoRes) {
    	logger.info("updateOrderStatus() → Updating order status for orderId: {}", orderId);
        PayoutRecords order = payoutRepository.findByOrderId(orderId);
        Map<String,Object> map = new HashMap<>();
        if (order == null) {
        	logger.warn("updateOrderStatus() → Order not found in DB for orderId: {}", orderId);
            map.put("status", "Error");
            map.put("message", "BAD_REQUEST");
            map.put("data", "Order Id not found..!");
            return map;
        }
        Object data = trexoRes.getData();
        logger.info("updateOrderStatus() → Trexo data received: {}", data);
        ObjectMapper mapper = new ObjectMapper();
        JSONObject json = new JSONObject(mapper.convertValue(data, Map.class));
        logger.info("updateOrderStatus() → Parsed JSON: {}", json);
        if (json.get("status").toString().equals("COMPLETED")) {
            order.setStatus("SUCCESS");
            order.setStatusCode("TXNS");
            if (json.has("utr")) {
                order.setUtr(json.get("utr").toString());
            }
            logger.info("updateOrderStatus() → Status updated to SUCCESS for orderId: {}", orderId);
        } else if (json.get("status").toString().equals("PROCESSING_REVERSAL")
                || json.get("status").toString().equals("PENDING")) {
            order.setStatus("PENDING");
            order.setStatusCode("TXNP");
            logger.info("updateOrderStatus() → Status updated to PENDING for orderId: {}", orderId);
        } else if (json.get("status").toString().equals("FAILED_REVERSED")) {
            order.setStatus("FAILED");
            order.setStatusCode("TXNF");
            logger.info("updateOrderStatus() → Status updated to FAILED for orderId: {}", orderId);
        }
        order.setUpdatedDate(LocalDateTime.now().toString());
        payoutRepository.save(order);
        logger.info("updateOrderStatus() → Order updated and saved for orderId: {}", orderId);
        map.put("order_id", orderId );
        map.put("transaction_id", json.get("transaction_id"));
        map.put("amount", json.get("amount"));
        map.put("utr", order.getUtr());
        map.put("status", order.getStatus());
        map.put("status_code", order.getStatusCode());
        map.put("refund_status", order.getRefundStatus());
        logger.info("updateOrderStatus() → Final response map prepared for orderId: {}", orderId);
        return map;

        //        String code = trexoRes.getCode();
        //        switch (code) {
        //            case "00":
        //                order.setStatus("SUCCESS");
        //                order.setStatusCode("TXNS");
        //                break;
        //
        //            case "01":
        //                order.setStatus("PENDING");
        //                order.setStatusCode("TXNP");
        //                break;
        //
        //            default:
        //                order.setStatus("FAILED");
        //                order.setStatusCode("TXNF");
        //                break;
        //        }
        //        try {
        //            Object dataObj = trexoRes.getData();
        //
        //            if (dataObj instanceof Map) {
        //                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
        //
        //                if (dataMap.containsKey("utr") && dataMap.get("utr") != null) {
        //                    order.setUtr(dataMap.get("utr").toString());
        //                }
        //            }
        //        } catch (Exception e) {
        //            System.out.println("Error extracting UTR: " + e.getMessage());
        //        }
        //        order.setErrorMsg(trexoRes.getMessage());
        //        order.setUpdatedDate(LocalDateTime.now().toString());
        //        payoutRepository.save(order);
    }

}
