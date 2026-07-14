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
@TableName("dzg_order")
public class ShopOrder extends TenantEntity {

    @TableId(value = "order_id")
    private Long orderId;

    private String orderNo;

    private Long customerId;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private String payType;

    private String payStatus;

    private Date orderTime;

    @TableLogic
    private String delFlag;

    private String remark;
}
