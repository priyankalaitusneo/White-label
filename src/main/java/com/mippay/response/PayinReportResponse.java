package com.mippay.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.mippay.dto.Admin.PayinReportDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayinReportResponse {
    private boolean success;
    private List<PayinReportDTO> data;
    private int currentPage;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
}