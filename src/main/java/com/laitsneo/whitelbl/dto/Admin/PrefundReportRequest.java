package com.laitsneo.whitelbl.dto.Admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class PrefundReportRequest {

    private String merchantId;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    // Pagination
    private Integer page;   // default 0
    private Integer size;   // default 10
}

