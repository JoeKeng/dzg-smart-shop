package com.dzg.shop.domain.bo;

import lombok.Data;

@Data
public class ShopStockAdjustBo {

    private Long productId;

    private Integer quantity;

    private String changeType;

    private String remark;
}
