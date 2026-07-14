package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_supplier")
public class ShopSupplier extends TenantEntity {

    @TableId(value = "supplier_id")
    private Long supplierId;

    private String supplierName;

    private String contactName;

    private String phone;

    private String address;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;

    @TableField(exist = false)
    private Long productCount;
}
