package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopOrder;
import com.dzg.shop.domain.ShopOrderItem;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.domain.vo.ShopDashboardVo;
import com.dzg.shop.domain.vo.ShopPaymentSummaryVo;
import com.dzg.shop.domain.vo.ShopReportVo;
import com.dzg.shop.mapper.ShopCreditRecordMapper;
import com.dzg.shop.mapper.ShopOrderItemMapper;
import com.dzg.shop.mapper.ShopOrderMapper;
import com.dzg.shop.mapper.ShopProductMapper;
import com.dzg.shop.mapper.ShopStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ShopReportService {

    private final ShopOrderMapper orderMapper;
    private final ShopOrderItemMapper orderItemMapper;
    private final ShopCreditRecordMapper creditRecordMapper;
    private final ShopStockMapper stockMapper;
    private final ShopProductMapper productMapper;

    public ShopDashboardVo dashboard() {
        DateRange todayRange = todayRange();
        List<ShopOrder> todayOrders = orderMapper.selectList(Wrappers.<ShopOrder>lambdaQuery()
            .ge(ShopOrder::getOrderTime, todayRange.begin())
            .lt(ShopOrder::getOrderTime, todayRange.end()));
        ShopDashboardVo vo = new ShopDashboardVo();
        vo.setTodayOrderCount((long) todayOrders.size());
        vo.setTodaySales(todayOrders.stream().map(ShopOrder::getPaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setTodayCredit(todayOrders.stream()
            .filter(order -> ShopConstants.CREDIT.equals(order.getPayType()))
            .map(ShopOrder::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setUnpaidTotal(unpaidTotal());
        vo.setLowStockCount(lowStockCount());
        return vo;
    }

    public ShopPaymentSummaryVo paymentSummary(String range) {
        DateRange dateRange = paymentDateRange(range);
        LambdaQueryWrapper<ShopOrder> query = Wrappers.lambdaQuery();
        if (dateRange.begin() != null) {
            query.ge(ShopOrder::getOrderTime, dateRange.begin());
        }
        if (dateRange.end() != null) {
            query.lt(ShopOrder::getOrderTime, dateRange.end());
        }
        List<ShopOrder> orders = orderMapper.selectList(query);
        List<ShopCreditRecord> credits = creditRecordsByOrders(orders);
        BigDecimal paidAmount = orders.stream().map(ShopOrder::getPaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditAmount = orders.stream()
            .filter(order -> ShopConstants.CREDIT.equals(order.getPayType()))
            .map(ShopOrder::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal repaidCreditAmount = credits.stream().map(ShopCreditRecord::getPaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unpaidAmount = credits.stream().map(ShopCreditRecord::getUnpaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        ShopPaymentSummaryVo vo = new ShopPaymentSummaryVo();
        vo.setRange(dateRange.range());
        vo.setOrderCount((long) orders.size());
        vo.setPaidAmount(paidAmount);
        vo.setCreditAmount(creditAmount);
        vo.setRepaidCreditAmount(repaidCreditAmount);
        vo.setUnpaidAmount(unpaidAmount);
        vo.setCollectedAmount(paidAmount.add(repaidCreditAmount));
        vo.setTotalAmount(paidAmount.add(creditAmount));
        return vo;
    }

    public ShopReportVo report() {
        return report("today");
    }

    public ShopReportVo report(String range) {
        DateRange dateRange = paymentDateRange(range);
        LambdaQueryWrapper<ShopOrder> query = Wrappers.lambdaQuery();
        if (dateRange.begin() != null) {
            query.ge(ShopOrder::getOrderTime, dateRange.begin());
        }
        if (dateRange.end() != null) {
            query.lt(ShopOrder::getOrderTime, dateRange.end());
        }
        List<ShopOrder> orders = orderMapper.selectList(query);
        List<ShopOrderItem> orderItems = orderItemsByOrders(orders);
        ShopReportVo vo = new ShopReportVo();
        vo.setOrderCount((long) orders.size());
        vo.setSalesTotal(orders.stream().map(ShopOrder::getPaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setCreditTotal(orders.stream()
            .filter(order -> ShopConstants.CREDIT.equals(order.getPayType()))
            .map(ShopOrder::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setUnpaidTotal(unpaidTotal());
        vo.setGrossProfit(grossProfit(orderItems));
        vo.setLowStockCount(lowStockCount());
        return vo;
    }

    private BigDecimal unpaidTotal() {
        return creditRecordMapper.selectList(Wrappers.<ShopCreditRecord>lambdaQuery().ne(ShopCreditRecord::getStatus, ShopConstants.SETTLED))
            .stream().map(ShopCreditRecord::getUnpaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long lowStockCount() {
        return stockMapper.selectList().stream()
            .filter(stock -> stock.getQuantity() != null && stock.getWarningQty() != null && stock.getQuantity() <= stock.getWarningQty())
            .count();
    }

    private List<ShopOrderItem> orderItemsByOrders(List<ShopOrder> orders) {
        List<Long> orderIds = orders.stream()
            .map(ShopOrder::getOrderId)
            .filter(Objects::nonNull)
            .toList();
        if (orderIds.isEmpty()) {
            return List.of();
        }
        return orderItemMapper.selectList(Wrappers.<ShopOrderItem>lambdaQuery().in(ShopOrderItem::getOrderId, orderIds));
    }

    private List<ShopCreditRecord> creditRecordsByOrders(List<ShopOrder> orders) {
        List<Long> orderIds = orders.stream()
            .map(ShopOrder::getOrderId)
            .filter(Objects::nonNull)
            .toList();
        if (orderIds.isEmpty()) {
            return List.of();
        }
        return creditRecordMapper.selectList(Wrappers.<ShopCreditRecord>lambdaQuery().in(ShopCreditRecord::getOrderId, orderIds));
    }

    private BigDecimal grossProfit(List<ShopOrderItem> orderItems) {
        BigDecimal profit = BigDecimal.ZERO;
        for (ShopOrderItem item : orderItems) {
            ShopProduct product = productMapper.selectById(item.getProductId());
            BigDecimal purchasePrice = product == null || product.getPurchasePrice() == null ? BigDecimal.ZERO : product.getPurchasePrice();
            BigDecimal salePrice = item.getSalePrice() == null ? BigDecimal.ZERO : item.getSalePrice();
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            profit = profit.add(salePrice.subtract(purchasePrice).multiply(BigDecimal.valueOf(quantity)));
        }
        return profit;
    }

    private DateRange todayRange() {
        LocalDate today = LocalDate.now();
        return dateRange("today", today, today.plusDays(1));
    }

    private DateRange paymentDateRange(String range) {
        LocalDate today = LocalDate.now();
        return switch (range == null ? "today" : range) {
            case "week" -> dateRange("week", today.minusDays(6), today.plusDays(1));
            case "month" -> dateRange("month", today.withDayOfMonth(1), today.plusDays(1));
            case "all" -> new DateRange("all", null, null);
            default -> todayRange();
        };
    }

    private DateRange dateRange(String range, LocalDate begin, LocalDate end) {
        ZoneId zoneId = ZoneId.systemDefault();
        return new DateRange(
            range,
            Date.from(begin.atStartOfDay(zoneId).toInstant()),
            Date.from(end.atStartOfDay(zoneId).toInstant())
        );
    }

    private record DateRange(String range, Date begin, Date end) {
    }
}
