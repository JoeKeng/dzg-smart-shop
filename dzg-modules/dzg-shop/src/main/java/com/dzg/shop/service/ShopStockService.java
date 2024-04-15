package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.domain.ShopStockLog;
import com.dzg.shop.domain.bo.ShopStockAdjustBo;
import com.dzg.shop.mapper.ShopProductMapper;
import com.dzg.shop.mapper.ShopStockLogMapper;
import com.dzg.shop.mapper.ShopStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShopStockService {

    private final ShopStockMapper stockMapper;
    private final ShopStockLogMapper stockLogMapper;
    private final ShopProductMapper productMapper;

    public TableDataInfo<ShopStock> stockPage(ShopStock query, PageQuery pageQuery) {
        LambdaQueryWrapper<ShopStock> lqw = Wrappers.lambdaQuery();
        lqw.eq(query.getProductId() != null, ShopStock::getProductId, query.getProductId());
        lqw.orderByDesc(ShopStock::getUpdateTime);
        Page<ShopStock> page = stockMapper.selectPage(pageQuery.build(), lqw);
        fillStockProductNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(ShopStockAdjustBo bo) {
        if (bo.getProductId() == null || bo.getQuantity() == null || bo.getQuantity() <= 0) {
            throw new ServiceException("请选择商品并输入大于 0 的数量");
        }
        String type = bo.getChangeType() == null || bo.getChangeType().isBlank() ? "in" : bo.getChangeType();
        changeStock(bo.getProductId(), bo.getQuantity(), type, null, bo.getRemark());
    }

    public TableDataInfo<ShopStockLog> stockLogPage(ShopStockLog query, PageQuery pageQuery) {
        LambdaQueryWrapper<ShopStockLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(query.getProductId() != null, ShopStockLog::getProductId, query.getProductId());
        lqw.orderByDesc(ShopStockLog::getCreateTime);
        Page<ShopStockLog> page = stockLogMapper.selectPage(pageQuery.build(), lqw);
        fillLogProductNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    public void ensureStock(Long productId, Integer warningQty) {
        ShopStock stock = getStock(productId);
        if (stock == null) {
            stock = new ShopStock();
            stock.setProductId(productId);
            stock.setQuantity(0);
            stock.setWarningQty(warningQty);
            stockMapper.insert(stock);
        } else {
            stock.setWarningQty(warningQty);
            stockMapper.updateById(stock);
        }
    }

    public void requireEnoughStock(Long productId, int quantity) {
        ShopStock stock = getStock(productId);
        if (stock == null || stock.getQuantity() == null || stock.getQuantity() < quantity) {
            throw new ServiceException("库存不足，不能收银");
        }
    }

    public void changeStock(Long productId, int quantity, String type, Long bizId, String remark) {
        ShopStock stock = getStock(productId);
        if (stock == null) {
            stock = new ShopStock();
            stock.setProductId(productId);
            stock.setQuantity(0);
            stock.setWarningQty(10);
            stockMapper.insert(stock);
        }
        int beforeQty = stock.getQuantity() == null ? 0 : stock.getQuantity();
        int delta = ("out".equals(type) || "sale".equals(type)) ? -quantity : quantity;
        int afterQty = beforeQty + delta;
        if (afterQty < 0) {
            throw new ServiceException("库存不足，不能出库");
        }
        stock.setQuantity(afterQty);
        stockMapper.updateById(stock);

        ShopStockLog log = new ShopStockLog();
        log.setProductId(productId);
        log.setChangeType(type);
        log.setChangeQty(delta);
        log.setBeforeQty(beforeQty);
        log.setAfterQty(afterQty);
        log.setBizId(bizId);
        log.setRemark(remark);
        stockLogMapper.insert(log);
    }

    public ShopStock getStock(Long productId) {
        return stockMapper.selectOne(Wrappers.<ShopStock>lambdaQuery().eq(ShopStock::getProductId, productId), false);
    }

    private Map<Long, ShopProduct> productMap(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return productMapper.selectByIds(productIds).stream()
            .collect(Collectors.toMap(ShopProduct::getProductId, Function.identity()));
    }

    private void fillStockProductNames(List<ShopStock> stocks) {
        List<Long> productIds = stocks.stream()
            .map(ShopStock::getProductId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, ShopProduct> products = productMap(productIds);
        stocks.forEach(stock -> {
            ShopProduct product = products.get(stock.getProductId());
            stock.setProductName(product == null ? null : product.getProductName());
        });
    }

    private void fillLogProductNames(List<ShopStockLog> logs) {
        List<Long> productIds = logs.stream()
            .map(ShopStockLog::getProductId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, ShopProduct> products = productMap(productIds);
        logs.forEach(log -> {
            ShopProduct product = products.get(log.getProductId());
            log.setProductName(product == null ? null : product.getProductName());
        });
    }
}
