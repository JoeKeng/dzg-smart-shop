package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

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

    private String imageOssId;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private Long supplierId;

    @TableField(exist = false)
    private List<Long> supplierIds;

    @TableField(exist = false)
    private String supplierNames;
}
