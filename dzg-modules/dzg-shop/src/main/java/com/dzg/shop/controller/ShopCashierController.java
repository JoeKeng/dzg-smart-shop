package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopOrder;
import com.dzg.shop.domain.bo.ShopOrderBo;
import com.dzg.shop.service.ShopCashierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ShopCashierController {

    private final ShopCashierService cashierService;

    @PostMapping("/order/cashier")
    public R<ShopOrder> cashier(@RequestBody ShopOrderBo bo) {
        return R.ok("已完成收银", cashierService.createOrder(bo));
    }

    @GetMapping("/order/list")
    public TableDataInfo<ShopOrder> orderPage(ShopOrder query, PageQuery pageQuery) {
        return cashierService.orderPage(query, pageQuery);
    }
}
