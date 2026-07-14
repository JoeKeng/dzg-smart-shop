package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_product_supplier")
public class ShopProductSupplier extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private Long productId;

    private Long supplierId;

    private Integer sortOrder;

    private String status;

    private String delFlag;
}
