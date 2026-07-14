package com.dzg.shop.domain.bo;

import lombok.Data;

import java.util.List;

@Data
public class ShopPurchaseBo {

    private Long supplierId;

    private String remark;

    private List<ShopPurchaseItemBo> items;
}
