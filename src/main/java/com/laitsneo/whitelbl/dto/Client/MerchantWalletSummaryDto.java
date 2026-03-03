package com.laitsneo.whitelbl.dto.Client;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantWalletSummaryDto {
  private String merchantId;
  private String merchantName;
  private BigDecimal totalFund;
  private BigDecimal available;
  private BigDecimal locked;
  private Long payinCount;
  private Long payoutCount;
}