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
@TableName("dzg_purchase_order")
public class ShopPurchaseOrder extends TenantEntity {

    @TableId(value = "purchase_id")
    private Long purchaseId;

    private String purchaseNo;

    private Long supplierId;

    private BigDecimal totalAmount;

    private String status;

    private Date purchaseTime;

    @TableLogic
    private String delFlag;

    private String remark;
}
