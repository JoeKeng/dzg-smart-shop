package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.core.utils.StringUtils;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopOrder;
import com.dzg.shop.domain.ShopOrderItem;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.domain.bo.ShopOrderBo;
import com.dzg.shop.domain.bo.ShopOrderItemBo;
import com.dzg.shop.mapper.ShopCreditRecordMapper;
import com.dzg.shop.mapper.ShopOrderItemMapper;
import com.dzg.shop.mapper.ShopOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class ShopCashierService {

    private final ShopProductService productService;
    private final ShopStockService stockService;
    private final ShopCreditService creditService;
    private final ShopOrderMapper orderMapper;
    private final ShopOrderItemMapper orderItemMapper;
    private final ShopCreditRecordMapper creditRecordMapper;

    @Transactional(rollbackFor = Exception.class)
    public ShopOrder createOrder(ShopOrderBo bo) {
        if (bo.getItems() == null || bo.getItems().isEmpty()) {
            throw new ServiceException("请先选择商品");
        }
        boolean creditPay = ShopConstants.CREDIT.equals(bo.getPayType());
        if (creditPay && bo.getCustomerId() == null) {
            throw new ServiceException("赊账收银必须选择客户");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ShopOrderItemBo item : bo.getItems()) {
            ShopProduct product = productService.requireProduct(item.getProductId());
            int qty = safeQty(item.getQuantity());
            total = total.add(product.getSalePrice().multiply(BigDecimal.valueOf(qty)));
            stockService.requireEnoughStock(product.getProductId(), qty);
        }

        ShopOrder order = new ShopOrder();
        order.setOrderNo("DZG" + System.currentTimeMillis());
        order.setCustomerId(bo.getCustomerId());
        order.setTotalAmount(total);
        order.setPayType(StringUtils.isBlank(bo.getPayType()) ? "cash" : bo.getPayType());
        order.setPaidAmount(creditPay ? BigDecimal.ZERO : defaultPaidAmount(bo.getPaidAmount(), total));
        order.setPayStatus(creditPay ? ShopConstants.UNPAID : ShopConstants.PAID);
        order.setOrderTime(new Date());
        order.setRemark(bo.getRemark());
        orderMapper.insert(order);

        for (ShopOrderItemBo item : bo.getItems()) {
            ShopProduct product = productService.requireProduct(item.getProductId());
            int qty = safeQty(item.getQuantity());
            BigDecimal subtotal = product.getSalePrice().multiply(BigDecimal.valueOf(qty));

            ShopOrderItem orderItem = new ShopOrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(product.getProductId());
            orderItem.setProductName(product.getProductName());
            orderItem.setQuantity(qty);
            orderItem.setSalePrice(product.getSalePrice());
            orderItem.setSubtotal(subtotal);
            orderItemMapper.insert(orderItem);

            stockService.changeStock(product.getProductId(), qty, "sale", order.getOrderId(), "收银扣减");
        }

        if (creditPay) {
            ShopCreditRecord credit = new ShopCreditRecord();
            credit.setOrderId(order.getOrderId());
            credit.setCustomerId(bo.getCustomerId());
            credit.setCreditAmount(total);
            credit.setPaidAmount(BigDecimal.ZERO);
            credit.setUnpaidAmount(total);
            credit.setStatus(ShopConstants.UNPAID);
            credit.setRemark(bo.getRemark());
            creditRecordMapper.insert(credit);
            creditService.rebuildCustomerDebt(bo.getCustomerId());
        }
        return order;
    }

    public TableDataInfo<ShopOrder> orderPage(ShopOrder query, PageQuery pageQuery) {
        LambdaQueryWrapper<ShopOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(query.getCustomerId() != null, ShopOrder::getCustomerId, query.getCustomerId());
        lqw.eq(StringUtils.isNotBlank(query.getPayType()), ShopOrder::getPayType, query.getPayType());
        lqw.orderByDesc(ShopOrder::getOrderTime);
        Page<ShopOrder> page = orderMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    private int safeQty(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ServiceException("商品数量必须大于 0");
        }
        return quantity;
    }

    private BigDecimal defaultPaidAmount(BigDecimal paidAmount, BigDecimal total) {
        return paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0 ? total : paidAmount;
    }
}
