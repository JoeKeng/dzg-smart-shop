package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.core.utils.StringUtils;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopCustomer;
import com.dzg.shop.mapper.ShopCustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ShopCustomerService {

    private final ShopCustomerMapper customerMapper;

    public TableDataInfo<ShopCustomer> customerPage(ShopCustomer query, PageQuery pageQuery) {
        Page<ShopCustomer> page = customerMapper.selectPage(pageQuery.build(), customerWrapper(query));
        return TableDataInfo.build(page);
    }

    public List<ShopCustomer> customerList(ShopCustomer query) {
        return customerMapper.selectList(customerWrapper(query));
    }

    public ShopCustomer getCustomer(Long customerId) {
        return customerMapper.selectById(customerId);
    }

    public void saveCustomer(ShopCustomer customer) {
        validatePhone(customer.getPhone());
        if (customer.getStatus() == null) {
            customer.setStatus(ShopConstants.NORMAL);
        }
        if (customer.getCreditLimit() == null) {
            customer.setCreditLimit(BigDecimal.ZERO);
        }
        if (customer.getCurrentDebt() == null) {
            customer.setCurrentDebt(BigDecimal.ZERO);
        }
        if (customer.getCustomerId() == null) {
            customerMapper.insert(customer);
        } else {
            customerMapper.updateById(customer);
        }
    }

    public void removeCustomer(List<Long> customerIds) {
        customerMapper.deleteByIds(customerIds);
    }

    private LambdaQueryWrapper<ShopCustomer> customerWrapper(ShopCustomer query) {
        LambdaQueryWrapper<ShopCustomer> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(query.getCustomerName()), ShopCustomer::getCustomerName, query.getCustomerName());
        lqw.like(StringUtils.isNotBlank(query.getPhone()), ShopCustomer::getPhone, query.getPhone());
        lqw.eq(StringUtils.isNotBlank(query.getStatus()), ShopCustomer::getStatus, query.getStatus());
        lqw.orderByDesc(ShopCustomer::getCreateTime);
        return lqw;
    }

    private void validatePhone(String phone) {
        if (StringUtils.isNotBlank(phone) && !phone.matches("\\d{11}")) {
            throw new ServiceException("手机号必须是11位数字");
        }
    }
}
