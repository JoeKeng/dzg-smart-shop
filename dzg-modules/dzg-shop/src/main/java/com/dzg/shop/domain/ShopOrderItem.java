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
@TableName("dzg_order_item")
public class ShopOrderItem extends TenantEntity {

    @TableId(value = "item_id")
    private Long itemId;

    private Long orderId;

    private Long productId;

    private String productName;

    private Integer quantity;

    private BigDecimal salePrice;

    private BigDecimal subtotal;

    @TableLogic
    private String delFlag;
}
