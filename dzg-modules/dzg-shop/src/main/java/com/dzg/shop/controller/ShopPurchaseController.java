package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopPurchaseOrder;
import com.dzg.shop.domain.bo.ShopPurchaseBo;
import com.dzg.shop.service.ShopPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ShopPurchaseController {

    private final ShopPurchaseService purchaseService;

    @PostMapping("/purchase")
    public R<ShopPurchaseOrder> createPurchase(@RequestBody ShopPurchaseBo bo) {
        return R.ok("采购入库已完成", purchaseService.createPurchase(bo));
    }

    @GetMapping("/purchase/list")
    public TableDataInfo<ShopPurchaseOrder> purchasePage(ShopPurchaseOrder query, PageQuery pageQuery) {
        return purchaseService.purchasePage(query, pageQuery);
    }
}
