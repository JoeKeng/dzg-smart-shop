package com.dzg.shop.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopPurchaseItemBo {

    private Long productId;

    private Integer quantity;

    private BigDecimal purchasePrice;
}
