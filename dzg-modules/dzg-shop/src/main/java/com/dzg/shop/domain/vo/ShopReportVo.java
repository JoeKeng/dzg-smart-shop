package com.dzg.shop.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopReportVo {

    private BigDecimal salesTotal = BigDecimal.ZERO;

    private Long orderCount = 0L;

    private BigDecimal creditTotal = BigDecimal.ZERO;

    private BigDecimal unpaidTotal = BigDecimal.ZERO;

    private BigDecimal grossProfit = BigDecimal.ZERO;

    private Long lowStockCount = 0L;
}
