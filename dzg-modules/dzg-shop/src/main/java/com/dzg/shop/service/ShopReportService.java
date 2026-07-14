package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopOrder;
import com.dzg.shop.domain.ShopOrderItem;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.domain.vo.ShopDashboardVo;
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
        Date begin = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<ShopOrder> todayOrders = orderMapper.selectList(Wrappers.<ShopOrder>lambdaQuery().ge(ShopOrder::getOrderTime, begin));
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

    public ShopReportVo report() {
        List<ShopOrder> orders = orderMapper.selectList(Wrappers.lambdaQuery());
        ShopReportVo vo = new ShopReportVo();
        vo.setOrderCount((long) orders.size());
        vo.setSalesTotal(orders.stream().map(ShopOrder::getPaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setCreditTotal(orders.stream()
            .filter(order -> ShopConstants.CREDIT.equals(order.getPayType()))
            .map(ShopOrder::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setUnpaidTotal(unpaidTotal());
        vo.setGrossProfit(grossProfit());
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

    private BigDecimal grossProfit() {
        BigDecimal profit = BigDecimal.ZERO;
        for (ShopOrderItem item : orderItemMapper.selectList(Wrappers.lambdaQuery())) {
            ShopProduct product = productMapper.selectById(item.getProductId());
            BigDecimal purchasePrice = product == null || product.getPurchasePrice() == null ? BigDecimal.ZERO : product.getPurchasePrice();
            BigDecimal salePrice = item.getSalePrice() == null ? BigDecimal.ZERO : item.getSalePrice();
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            profit = profit.add(salePrice.subtract(purchasePrice).multiply(BigDecimal.valueOf(quantity)));
        }
        return profit;
    }
}
