package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_repayment")
public class ShopRepayment extends TenantEntity {

    @TableId(value = "repayment_id")
    private Long repaymentId;

    private Long creditId;

    private Long customerId;

    private BigDecimal repaymentAmount;

    private String payType;

    private Date repaymentTime;

    @TableLogic
    private String delFlag;

    private String remark;
}
