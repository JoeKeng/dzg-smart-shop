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
@TableName("dzg_product")
public class ShopProduct extends TenantEntity {

    @TableId(value = "product_id")
    private Long productId;

    private Long categoryId;

    private String productName;

    private String barcode;

    private String unitName;

    private BigDecimal salePrice;

    private BigDecimal purchasePrice;

    private Integer warningQty;

    private String imageUrl;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;
}
