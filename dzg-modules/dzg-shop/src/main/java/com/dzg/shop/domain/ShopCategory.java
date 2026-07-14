package com.dzg.shop.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dzg.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dzg_category")
public class ShopCategory extends TenantEntity {

    @TableId(value = "category_id")
    private Long categoryId;

    private String categoryName;

    private Integer sortOrder;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;
}
