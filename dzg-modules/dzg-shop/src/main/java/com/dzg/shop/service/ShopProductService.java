package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dzg.common.core.constant.TenantConstants;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.core.utils.StringUtils;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopCategory;
import com.dzg.shop.domain.ShopOrderItem;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.domain.ShopProductSupplier;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.domain.ShopSupplier;
import com.dzg.shop.mapper.ShopCategoryMapper;
import com.dzg.shop.mapper.ShopOrderItemMapper;
import com.dzg.shop.mapper.ShopProductMapper;
import com.dzg.shop.mapper.ShopProductSupplierMapper;
import com.dzg.shop.mapper.ShopSupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RequiredArgsConstructor
@Service
public class ShopProductService {


    private final ShopCategoryMapper categoryMapper;
    private final ShopProductMapper productMapper;
    private final ShopProductSupplierMapper productSupplierMapper;
    private final ShopSupplierMapper supplierMapper;
    private final ShopOrderItemMapper orderItemMapper;
    private final ShopStockService stockService;

    public TableDataInfo<ShopCategory> categoryPage(ShopCategory query, PageQuery pageQuery) {
        LambdaQueryWrapper<ShopCategory> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(query.getCategoryName()), ShopCategory::getCategoryName, query.getCategoryName());
        lqw.orderByAsc(ShopCategory::getSortOrder).orderByDesc(ShopCategory::getCreateTime);
        Page<ShopCategory> page = categoryMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    public List<ShopCategory> categoryList() {
        return categoryMapper.selectList(Wrappers.<ShopCategory>lambdaQuery()
            .eq(ShopCategory::getStatus, ShopConstants.NORMAL)
            .orderByAsc(ShopCategory::getSortOrder));
    }

    public void saveCategory(ShopCategory category) {
        fillShopDefaults(category);
        if (category.getStatus() == null) {
            category.setStatus(ShopConstants.NORMAL);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getCategoryId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
    }

    public void removeCategory(List<Long> categoryIds) {
        categoryMapper.deleteByIds(categoryIds);
    }

    public TableDataInfo<ShopProduct> productPage(ShopProduct query, PageQuery pageQuery) {
        Page<ShopProduct> page = productMapper.selectPage(pageQuery.build(), productWrapper(query));
        fillProductExtras(page.getRecords());
        return TableDataInfo.build(page);
    }

    public List<ShopProduct> productList(ShopProduct query) {
        List<ShopProduct> list = productMapper.selectList(productWrapper(query));
        fillProductExtras(list);
        return list;
    }

    public ShopProduct getProduct(Long productId) {
        ShopProduct product = productMapper.selectById(productId);
        fillProductExtras(product == null ? Collections.emptyList() : List.of(product));
        return product;
    }

    public ShopProduct requireProduct(Long productId) {
        if (productId == null) {
            throw new ServiceException("商品不能为空");
        }
        ShopProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        if (product.getSalePrice() == null) {
            throw new ServiceException("商品未设置售价");
        }
        return product;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(ShopProduct product) {
        fillShopDefaults(product);
        if (product.getStatus() == null) {
            product.setStatus(ShopConstants.NORMAL);
        }
        if (product.getUnitName() == null) {
            product.setUnitName("件");
        }
        if (product.getWarningQty() == null) {
            product.setWarningQty(10);
        }
        if (product.getProductId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.updateById(product);
        }
        syncProductSuppliers(product.getProductId(), product.getSupplierIds());
        stockService.ensureStock(product.getProductId(), product.getWarningQty());
    }

    public void removeProduct(List<Long> productIds) {
        productMapper.deleteByIds(productIds);
    }

    private LambdaQueryWrapper<ShopProduct> productWrapper(ShopProduct query) {
        final ShopProduct q = (query == null) ? new ShopProduct() : query;
        LambdaQueryWrapper<ShopProduct> lqw = Wrappers.lambdaQuery();
        lqw.and(StringUtils.isNotBlank(q.getKeyword()), wrapper -> wrapper
            .like(ShopProduct::getProductName, q.getKeyword())
            .or()
            .like(ShopProduct::getBarcode, q.getKeyword()));
        lqw.like(StringUtils.isNotBlank(q.getProductName()), ShopProduct::getProductName, q.getProductName());
        lqw.eq(q.getCategoryId() != null, ShopProduct::getCategoryId, q.getCategoryId());
        lqw.like(StringUtils.isNotBlank(q.getBarcode()), ShopProduct::getBarcode, q.getBarcode());
        lqw.eq(StringUtils.isNotBlank(q.getStatus()), ShopProduct::getStatus, q.getStatus());
        if (q.getSupplierId() != null) {
            List<Long> productIds = productSupplierMapper.selectList(Wrappers.<ShopProductSupplier>lambdaQuery()
                    .eq(ShopProductSupplier::getSupplierId, q.getSupplierId())
                    .and(wrapper -> wrapper.eq(ShopProductSupplier::getStatus, ShopConstants.NORMAL)
                        .or()
                        .isNull(ShopProductSupplier::getStatus))
                    .and(wrapper -> wrapper.eq(ShopProductSupplier::getDelFlag, ShopConstants.NORMAL)
                        .or()
                        .isNull(ShopProductSupplier::getDelFlag)))
                .stream()
                .map(ShopProductSupplier::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            if (productIds.isEmpty()) {
                lqw.eq(ShopProduct::getProductId, -1L);
            } else {
                lqw.in(ShopProduct::getProductId, productIds);
            }
        }
        if (Boolean.TRUE.equals(q.getSortBySales())) {
            lqw.last("order by (select coalesce(sum(oi.quantity), 0) from dzg_order_item oi where oi.product_id = dzg_product.product_id and (oi.del_flag = '0' or oi.del_flag is null)) desc, create_time desc");
        } else {
            lqw.orderByDesc(ShopProduct::getCreateTime);
        }
        return lqw;
    }

    private void syncProductSuppliers(Long productId, List<Long> supplierIds) {
        productSupplierMapper.delete(Wrappers.<ShopProductSupplier>lambdaQuery()
            .eq(ShopProductSupplier::getProductId, productId));
        if (supplierIds == null || supplierIds.isEmpty()) {
            return;
        }
        Set<Long> distinctIds = supplierIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        int sortOrder = 0;
        for (Long supplierId : distinctIds) {
            ShopProductSupplier relation = new ShopProductSupplier();
            fillShopDefaults(relation);
            relation.setProductId(productId);
            relation.setSupplierId(supplierId);
            relation.setSortOrder(sortOrder++);
            relation.setStatus(ShopConstants.NORMAL);
            relation.setDelFlag(ShopConstants.NORMAL);
            productSupplierMapper.insert(relation);
        }
    }

    public Long countBySupplierId(Long supplierId) {
        return productSupplierMapper.selectCount(Wrappers.<ShopProductSupplier>lambdaQuery()
            .eq(ShopProductSupplier::getSupplierId, supplierId)
            .and(wrapper -> wrapper.eq(ShopProductSupplier::getStatus, ShopConstants.NORMAL)
                .or()
                .isNull(ShopProductSupplier::getStatus))
            .and(wrapper -> wrapper.eq(ShopProductSupplier::getDelFlag, ShopConstants.NORMAL)
                .or()
                .isNull(ShopProductSupplier::getDelFlag)));
    }

    private void fillProductExtras(List<ShopProduct> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        List<Long> categoryIds = products.stream()
            .map(ShopProduct::getCategoryId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, ShopCategory> categoryMap = categoryIds.isEmpty()
            ? Collections.emptyMap()
            : categoryMapper.selectByIds(categoryIds).stream().collect(Collectors.toMap(ShopCategory::getCategoryId, Function.identity()));

        List<Long> productIds = products.stream()
            .map(ShopProduct::getProductId)
            .filter(Objects::nonNull)
            .toList();
        List<ShopProductSupplier> relations = productSupplierMapper.selectList(Wrappers.<ShopProductSupplier>lambdaQuery()
            .in(ShopProductSupplier::getProductId, productIds)
            .and(wrapper -> wrapper.eq(ShopProductSupplier::getStatus, ShopConstants.NORMAL)
                .or()
                .isNull(ShopProductSupplier::getStatus))
            .and(wrapper -> wrapper.eq(ShopProductSupplier::getDelFlag, ShopConstants.NORMAL)
                .or()
                .isNull(ShopProductSupplier::getDelFlag))
            .orderByAsc(ShopProductSupplier::getSortOrder));
        List<Long> supplierIds = relations.stream()
            .map(ShopProductSupplier::getSupplierId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, ShopSupplier> supplierMap = supplierIds.isEmpty()
            ? Collections.emptyMap()
            : supplierMapper.selectByIds(supplierIds).stream().collect(Collectors.toMap(ShopSupplier::getSupplierId, Function.identity()));
        Map<Long, List<ShopProductSupplier>> relationMap = relations.stream()
            .collect(Collectors.groupingBy(ShopProductSupplier::getProductId));
        Map<Long, Integer> saleCountMap = orderItemMapper.selectMaps(new QueryWrapper<ShopOrderItem>()
                .select("product_id", "coalesce(sum(quantity), 0) as sale_count")
                .in("product_id", productIds)
                .and(wrapper -> wrapper.eq("del_flag", ShopConstants.NORMAL).or().isNull("del_flag"))
                .groupBy("product_id"))
            .stream()
            .collect(Collectors.toMap(
                row -> Long.valueOf(String.valueOf(row.get("product_id"))),
                row -> {
                    Object value = row.getOrDefault("sale_count", row.get("SALE_COUNT"));
                    return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
                }
            ));

        for (ShopProduct product : products) {
            ShopCategory category = categoryMap.get(product.getCategoryId());
            product.setCategoryName(category == null ? null : category.getCategoryName());
            List<ShopProductSupplier> productRelations = relationMap.getOrDefault(product.getProductId(), Collections.emptyList());
            List<Long> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (ShopProductSupplier relation : productRelations) {
                if (relation.getSupplierId() == null) {
                    continue;
                }
                ids.add(relation.getSupplierId());
                ShopSupplier supplier = supplierMap.get(relation.getSupplierId());
                if (supplier != null) {
                    names.add(supplier.getSupplierName());
                } else {
                    names.add("供应商ID " + relation.getSupplierId());
                }
            }
            product.setSupplierIds(ids);
            product.setSupplierNames(String.join("、", names));
            product.setSaleCount(saleCountMap.getOrDefault(product.getProductId(), 0));
            ShopStock stock = stockService.getStock(product.getProductId());
            product.setStockQuantity(stock == null || stock.getQuantity() == null ? 0 : stock.getQuantity());
        }
    }

    private void fillShopDefaults(ShopCategory category) {
        if (StringUtils.isBlank(category.getTenantId())) {
            category.setTenantId(TenantConstants.DEFAULT_TENANT_ID);
        }
        if (category.getDelFlag() == null) {
            category.setDelFlag(ShopConstants.NORMAL);
        }
    }

    private void fillShopDefaults(ShopProduct product) {
        if (StringUtils.isBlank(product.getTenantId())) {
            product.setTenantId(TenantConstants.DEFAULT_TENANT_ID);
        }
        if (product.getDelFlag() == null) {
            product.setDelFlag(ShopConstants.NORMAL);
        }
    }

    private void fillShopDefaults(ShopProductSupplier relation) {
        if (StringUtils.isBlank(relation.getTenantId())) {
            relation.setTenantId(TenantConstants.DEFAULT_TENANT_ID);
        }
        if (relation.getStatus() == null) {
            relation.setStatus(ShopConstants.NORMAL);
        }
        if (relation.getDelFlag() == null) {
            relation.setDelFlag(ShopConstants.NORMAL);
        }
    }
}
