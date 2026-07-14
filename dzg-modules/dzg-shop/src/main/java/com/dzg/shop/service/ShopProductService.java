package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.core.utils.StringUtils;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopCategory;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.mapper.ShopCategoryMapper;
import com.dzg.shop.mapper.ShopProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ShopProductService {

    private final ShopCategoryMapper categoryMapper;
    private final ShopProductMapper productMapper;
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
        return TableDataInfo.build(page);
    }

    public List<ShopProduct> productList(ShopProduct query) {
        return productMapper.selectList(productWrapper(query));
    }

    public ShopProduct getProduct(Long productId) {
        return productMapper.selectById(productId);
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
        stockService.ensureStock(product.getProductId(), product.getWarningQty());
    }

    public void removeProduct(List<Long> productIds) {
        productMapper.deleteByIds(productIds);
    }

    private LambdaQueryWrapper<ShopProduct> productWrapper(ShopProduct query) {
        LambdaQueryWrapper<ShopProduct> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(query.getProductName()), ShopProduct::getProductName, query.getProductName());
        lqw.eq(query.getCategoryId() != null, ShopProduct::getCategoryId, query.getCategoryId());
        lqw.like(StringUtils.isNotBlank(query.getBarcode()), ShopProduct::getBarcode, query.getBarcode());
        lqw.eq(StringUtils.isNotBlank(query.getStatus()), ShopProduct::getStatus, query.getStatus());
        lqw.orderByDesc(ShopProduct::getCreateTime);
        return lqw;
    }
}
