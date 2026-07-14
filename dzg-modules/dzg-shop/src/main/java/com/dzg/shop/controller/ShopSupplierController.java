package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopSupplier;
import com.dzg.shop.service.ShopSupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RequiredArgsConstructor
@RestController
public class ShopSupplierController {

    private final ShopSupplierService supplierService;

    @GetMapping("/supplier/list")
    public TableDataInfo<ShopSupplier> supplierPage(ShopSupplier query, PageQuery pageQuery) {
        return supplierService.supplierPage(query, pageQuery);
    }

    @GetMapping("/supplier/options")
    public R<?> supplierOptions(ShopSupplier query) {
        return R.ok(supplierService.supplierList(query));
    }

    @PostMapping("/supplier")
    public R<Void> addSupplier(@RequestBody ShopSupplier supplier) {
        supplierService.saveSupplier(supplier);
        return R.ok();
    }

    @PutMapping("/supplier")
    public R<Void> editSupplier(@RequestBody ShopSupplier supplier) {
        supplierService.saveSupplier(supplier);
        return R.ok();
    }

    @DeleteMapping("/supplier/{supplierIds}")
    public R<Void> removeSupplier(@PathVariable Long[] supplierIds) {
        supplierService.removeSupplier(Arrays.asList(supplierIds));
        return R.ok();
    }
}
