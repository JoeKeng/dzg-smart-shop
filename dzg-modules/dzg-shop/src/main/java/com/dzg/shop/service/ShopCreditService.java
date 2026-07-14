package com.dzg.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dzg.common.core.exception.ServiceException;
import com.dzg.common.core.utils.StringUtils;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopCustomer;
import com.dzg.shop.domain.ShopRepayment;
import com.dzg.shop.domain.bo.ShopRepaymentBo;
import com.dzg.shop.mapper.ShopCreditRecordMapper;
import com.dzg.shop.mapper.ShopCustomerMapper;
import com.dzg.shop.mapper.ShopRepaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ShopCreditService {

    private final ShopCreditRecordMapper creditRecordMapper;
    private final ShopRepaymentMapper repaymentMapper;
    private final ShopCustomerMapper customerMapper;

    public TableDataInfo<ShopCreditRecord> creditPage(ShopCreditRecord query, PageQuery pageQuery) {
        LambdaQueryWrapper<ShopCreditRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(query.getCustomerId() != null, ShopCreditRecord::getCustomerId, query.getCustomerId());
        lqw.eq(StringUtils.isNotBlank(query.getStatus()), ShopCreditRecord::getStatus, query.getStatus());
        lqw.orderByDesc(ShopCreditRecord::getCreateTime);
        Page<ShopCreditRecord> page = creditRecordMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public void repay(ShopRepaymentBo bo) {
        if (bo.getCreditId() == null || bo.getRepaymentAmount() == null || bo.getRepaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("请选择赊账记录并输入还款金额");
        }
        ShopCreditRecord credit = creditRecordMapper.selectById(bo.getCreditId());
        if (credit == null) {
            throw new ServiceException("赊账记录不存在");
        }
        if (ShopConstants.SETTLED.equals(credit.getStatus())) {
            throw new ServiceException("该赊账已结清");
        }

        BigDecimal repaymentAmount = bo.getRepaymentAmount().min(credit.getUnpaidAmount());
        ShopRepayment repayment = new ShopRepayment();
        repayment.setCreditId(credit.getCreditId());
        repayment.setCustomerId(credit.getCustomerId());
        repayment.setRepaymentAmount(repaymentAmount);
        repayment.setPayType(bo.getPayType());
        repayment.setRepaymentTime(new Date());
        repayment.setRemark(bo.getRemark());
        repaymentMapper.insert(repayment);

        BigDecimal paidAmount = credit.getPaidAmount().add(repaymentAmount);
        BigDecimal unpaidAmount = credit.getCreditAmount().subtract(paidAmount).max(BigDecimal.ZERO);
        credit.setPaidAmount(paidAmount);
        credit.setUnpaidAmount(unpaidAmount);
        credit.setStatus(unpaidAmount.compareTo(BigDecimal.ZERO) == 0 ? ShopConstants.SETTLED : ShopConstants.PARTIAL);
        creditRecordMapper.updateById(credit);
        rebuildCustomerDebt(credit.getCustomerId());
    }

    public TableDataInfo<ShopRepayment> repaymentPage(ShopRepayment query, PageQuery pageQuery) {
        LambdaQueryWrapper<ShopRepayment> lqw = Wrappers.lambdaQuery();
        lqw.eq(query.getCreditId() != null, ShopRepayment::getCreditId, query.getCreditId());
        lqw.eq(query.getCustomerId() != null, ShopRepayment::getCustomerId, query.getCustomerId());
        lqw.orderByDesc(ShopRepayment::getRepaymentTime);
        Page<ShopRepayment> page = repaymentMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    public void rebuildCustomerDebt(Long customerId) {
        BigDecimal debt = creditRecordMapper.selectList(Wrappers.<ShopCreditRecord>lambdaQuery()
                .eq(ShopCreditRecord::getCustomerId, customerId)
                .ne(ShopCreditRecord::getStatus, ShopConstants.SETTLED))
            .stream()
            .map(ShopCreditRecord::getUnpaidAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        ShopCustomer customer = customerMapper.selectById(customerId);
        if (customer != null) {
            customer.setCurrentDebt(debt);
            customerMapper.updateById(customer);
        }
    }
}
