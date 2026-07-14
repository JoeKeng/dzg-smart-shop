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
@TableName("dzg_credit_record")
public class ShopCreditRecord extends TenantEntity {

    @TableId(value = "credit_id")
    private Long creditId;

    private Long orderId;

    private Long customerId;

    private BigDecimal creditAmount;

    private BigDecimal paidAmount;

    private BigDecimal unpaidAmount;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;
}
