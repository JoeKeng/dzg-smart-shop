package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_stock")
public class ShopStock extends TenantEntity {

    @TableId(value = "stock_id")
    private Long stockId;

    private Long productId;

    private Integer quantity;

    private Integer warningQty;

    @TableLogic
    private String delFlag;
}
