package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopCreditRecord;
import com.dzg.shop.domain.ShopRepayment;
import com.dzg.shop.domain.bo.ShopRepaymentBo;
import com.dzg.shop.service.ShopCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ShopCreditController {

    private final ShopCreditService creditService;

    @GetMapping("/credit/list")
    public TableDataInfo<ShopCreditRecord> creditPage(ShopCreditRecord query, PageQuery pageQuery) {
        return creditService.creditPage(query, pageQuery);
    }

    @PostMapping("/credit/repay")
    public R<Void> repay(@RequestBody ShopRepaymentBo bo) {
        creditService.repay(bo);
        return R.ok("还款已登记");
    }

    @GetMapping("/repayment/list")
    public TableDataInfo<ShopRepayment> repaymentPage(ShopRepayment query, PageQuery pageQuery) {
        return creditService.repaymentPage(query, pageQuery);
    }
}
