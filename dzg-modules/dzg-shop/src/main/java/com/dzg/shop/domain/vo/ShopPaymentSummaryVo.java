package com.dzg.shop.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopPaymentSummaryVo {

    private String range;

    private Long orderCount = 0L;

    private BigDecimal paidAmount = BigDecimal.ZERO;

    private BigDecimal creditAmount = BigDecimal.ZERO;

    private BigDecimal repaidCreditAmount = BigDecimal.ZERO;

    private BigDecimal unpaidAmount = BigDecimal.ZERO;

    private BigDecimal collectedAmount = BigDecimal.ZERO;

    private BigDecimal totalAmount = BigDecimal.ZERO;
}
