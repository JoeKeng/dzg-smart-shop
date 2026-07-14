package com.dzg.shop.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopRepaymentBo {

    private Long creditId;

    private BigDecimal repaymentAmount;

    private String payType;

    private String remark;
}
