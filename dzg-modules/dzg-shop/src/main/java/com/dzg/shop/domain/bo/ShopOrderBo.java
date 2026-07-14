package com.dzg.shop.domain.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShopOrderBo {

    private Long customerId;

    private String payType;

    private BigDecimal paidAmount;

    private String remark;

    private List<ShopOrderItemBo> items = new ArrayList<>();
}
