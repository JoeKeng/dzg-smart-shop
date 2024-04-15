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
@TableName("dzg_stock_log")
public class ShopStockLog extends TenantEntity {

    @TableId(value = "log_id")
    private Long logId;

    private Long productId;

    private String changeType;

    private Integer changeQty;

    private Integer beforeQty;

    private Integer afterQty;

    private Long bizId;

    @TableLogic
    private String delFlag;

    private String remark;

    @TableField(exist = false)
    private String productName;
}
