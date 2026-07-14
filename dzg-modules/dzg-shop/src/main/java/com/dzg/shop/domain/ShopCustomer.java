package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_customer")
public class ShopCustomer extends TenantEntity {

    @TableId(value = "customer_id")
    private Long customerId;

    private String customerName;

    private String phone;

    private BigDecimal creditLimit;

    private BigDecimal currentDebt;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;
}
