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
@TableName("dzg_purchase_item")
public class ShopPurchaseItem extends TenantEntity {

    @TableId(value = "item_id")
    private Long itemId;

    private Long purchaseId;

    private Long productId;

    private String productName;

    private Integer quantity;

    private BigDecimal purchasePrice;

    private BigDecimal subtotal;

    @TableLogic
    private String delFlag;
}
