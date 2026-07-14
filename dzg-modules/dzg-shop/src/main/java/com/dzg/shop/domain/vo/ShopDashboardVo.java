package com.dzg.shop.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopDashboardVo {

    private BigDecimal todaySales = BigDecimal.ZERO;

    private Long todayOrderCount = 0L;

    private BigDecimal todayCredit = BigDecimal.ZERO;

    private BigDecimal unpaidTotal = BigDecimal.ZERO;

    private Long lowStockCount = 0L;
}
