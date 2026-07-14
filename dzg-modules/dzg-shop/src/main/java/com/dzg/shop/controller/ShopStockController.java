package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopStock;
import com.dzg.shop.domain.ShopStockLog;
import com.dzg.shop.domain.bo.ShopStockAdjustBo;
import com.dzg.shop.service.ShopStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ShopStockController {

    private final ShopStockService stockService;

    @GetMapping("/stock/list")
    public TableDataInfo<ShopStock> stockPage(ShopStock query, PageQuery pageQuery) {
        return stockService.stockPage(query, pageQuery);
    }

    @PostMapping("/stock/adjust")
    public R<Void> adjustStock(@RequestBody ShopStockAdjustBo bo) {
        stockService.adjustStock(bo);
        return R.ok("库存已更新");
    }

    @GetMapping("/stock/log/list")
    public TableDataInfo<ShopStockLog> stockLogPage(ShopStockLog query, PageQuery pageQuery) {
        return stockService.stockLogPage(query, pageQuery);
    }
}
