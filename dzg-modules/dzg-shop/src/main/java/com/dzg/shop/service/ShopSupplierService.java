package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dzg.common.core.constant.TenantConstants;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.core.utils.StringUtils;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopSupplier;
import com.dzg.shop.mapper.ShopSupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ShopSupplierService {

    private final ShopSupplierMapper supplierMapper;
    private final ShopProductService productService;

    public TableDataInfo<ShopSupplier> supplierPage(ShopSupplier query, PageQuery pageQuery) {
        Page<ShopSupplier> page = supplierMapper.selectPage(pageQuery.build(), supplierWrapper(query));
        page.getRecords().forEach(supplier -> supplier.setProductCount(productService.countBySupplierId(supplier.getSupplierId())));
        return TableDataInfo.build(page);
    }

    public List<ShopSupplier> supplierList(ShopSupplier query) {
        return supplierMapper.selectList(supplierWrapper(query));
    }

    public void saveSupplier(ShopSupplier supplier) {
        validatePhone(supplier.getPhone());
        if (StringUtils.isBlank(supplier.getTenantId())) {
            supplier.setTenantId(TenantConstants.DEFAULT_TENANT_ID);
        }
        if (supplier.getStatus() == null) {
            supplier.setStatus(ShopConstants.NORMAL);
        }
        if (supplier.getDelFlag() == null) {
            supplier.setDelFlag(ShopConstants.NORMAL);
        }
        if (supplier.getSupplierId() == null) {
            supplierMapper.insert(supplier);
        } else {
            supplierMapper.updateById(supplier);
        }
    }

    public void removeSupplier(List<Long> supplierIds) {
        supplierMapper.deleteByIds(supplierIds);
    }

    private LambdaQueryWrapper<ShopSupplier> supplierWrapper(ShopSupplier query) {
        if (query == null) {
            query = new ShopSupplier();
        }
        LambdaQueryWrapper<ShopSupplier> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(query.getSupplierName()), ShopSupplier::getSupplierName, query.getSupplierName());
        lqw.like(StringUtils.isNotBlank(query.getPhone()), ShopSupplier::getPhone, query.getPhone());
        if (ShopConstants.NORMAL.equals(query.getStatus())) {
            lqw.and(wrapper -> wrapper.eq(ShopSupplier::getStatus, ShopConstants.NORMAL)
                .or()
                .isNull(ShopSupplier::getStatus));
        } else {
            lqw.eq(StringUtils.isNotBlank(query.getStatus()), ShopSupplier::getStatus, query.getStatus());
        }
        lqw.orderByDesc(ShopSupplier::getCreateTime);
        return lqw;
    }

    private void validatePhone(String phone) {
        if (StringUtils.isNotBlank(phone) && !phone.matches("\\d{11}")) {
            throw new ServiceException("手机号必须是11位数字");
        }
    }
}
