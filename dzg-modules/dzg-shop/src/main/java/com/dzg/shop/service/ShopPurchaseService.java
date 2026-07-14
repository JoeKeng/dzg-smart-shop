package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.domain.ShopPurchaseItem;
import com.dzg.shop.domain.ShopPurchaseOrder;
import com.dzg.shop.domain.bo.ShopPurchaseBo;
import com.dzg.shop.domain.bo.ShopPurchaseItemBo;
import com.dzg.shop.mapper.ShopProductMapper;
import com.dzg.shop.mapper.ShopPurchaseItemMapper;
import com.dzg.shop.mapper.ShopPurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class ShopPurchaseService {

    private final ShopProductService productService;
    private final ShopStockService stockService;
    private final ShopProductMapper productMapper;
    private final ShopPurchaseOrderMapper purchaseOrderMapper;
    private final ShopPurchaseItemMapper purchaseItemMapper;

    @Transactional(rollbackFor = Exception.class)
    public ShopPurchaseOrder createPurchase(ShopPurchaseBo bo) {
        if (bo.getItems() == null || bo.getItems().isEmpty()) {
            throw new ServiceException("请先选择入库商品");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ShopPurchaseItemBo item : bo.getItems()) {
            ShopProduct product = productService.requireProduct(item.getProductId());
            int qty = safeQty(item.getQuantity());
            BigDecimal price = defaultPurchasePrice(item.getPurchasePrice(), product.getPurchasePrice());
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        ShopPurchaseOrder order = new ShopPurchaseOrder();
        order.setPurchaseNo("CGRK" + System.currentTimeMillis());
        order.setSupplierId(bo.getSupplierId());
        order.setTotalAmount(total);
        order.setStatus("done");
        order.setPurchaseTime(new Date());
        order.setRemark(bo.getRemark());
        purchaseOrderMapper.insert(order);

        for (ShopPurchaseItemBo item : bo.getItems()) {
            ShopProduct product = productService.requireProduct(item.getProductId());
            int qty = safeQty(item.getQuantity());
            BigDecimal price = defaultPurchasePrice(item.getPurchasePrice(), product.getPurchasePrice());

            ShopPurchaseItem purchaseItem = new ShopPurchaseItem();
            purchaseItem.setPurchaseId(order.getPurchaseId());
            purchaseItem.setProductId(product.getProductId());
            purchaseItem.setProductName(product.getProductName());
            purchaseItem.setQuantity(qty);
            purchaseItem.setPurchasePrice(price);
            purchaseItem.setSubtotal(price.multiply(BigDecimal.valueOf(qty)));
            purchaseItemMapper.insert(purchaseItem);

            if (product.getPurchasePrice() == null || price.compareTo(product.getPurchasePrice()) != 0) {
                product.setPurchasePrice(price);
                productMapper.updateById(product);
            }
            stockService.changeStock(product.getProductId(), qty, "purchase", order.getPurchaseId(), "采购入库");
        }
        return order;
    }

    public TableDataInfo<ShopPurchaseOrder> purchasePage(ShopPurchaseOrder query, PageQuery pageQuery) {
        LambdaQueryWrapper<ShopPurchaseOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(query.getSupplierId() != null, ShopPurchaseOrder::getSupplierId, query.getSupplierId());
        lqw.orderByDesc(ShopPurchaseOrder::getPurchaseTime);
        Page<ShopPurchaseOrder> page = purchaseOrderMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    private int safeQty(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ServiceException("入库数量必须大于 0");
        }
        return quantity;
    }

    private BigDecimal defaultPurchasePrice(BigDecimal inputPrice, BigDecimal productPrice) {
        if (inputPrice != null && inputPrice.compareTo(BigDecimal.ZERO) >= 0) {
            return inputPrice;
        }
        return productPrice == null ? BigDecimal.ZERO : productPrice;
    }
}
