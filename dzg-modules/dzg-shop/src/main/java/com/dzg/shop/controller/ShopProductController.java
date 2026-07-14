package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopCategory;
import com.dzg.shop.domain.ShopProduct;
import com.dzg.shop.service.ShopProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RequiredArgsConstructor
@RestController
public class ShopProductController {

    private final ShopProductService productService;

    @GetMapping("/category/list")
    public TableDataInfo<ShopCategory> categoryList(ShopCategory query, PageQuery pageQuery) {
        return productService.categoryPage(query, pageQuery);
    }

    @GetMapping("/category/options")
    public R<?> categoryOptions() {
        return R.ok(productService.categoryList());
    }

    @PostMapping("/category")
    public R<Void> addCategory(@RequestBody ShopCategory category) {
        productService.saveCategory(category);
        return R.ok();
    }

    @PutMapping("/category")
    public R<Void> editCategory(@RequestBody ShopCategory category) {
        productService.saveCategory(category);
        return R.ok();
    }

    @DeleteMapping("/category/{categoryIds}")
    public R<Void> removeCategory(@PathVariable Long[] categoryIds) {
        productService.removeCategory(Arrays.asList(categoryIds));
        return R.ok();
    }

    @GetMapping("/product/list")
    public TableDataInfo<ShopProduct> productPage(ShopProduct query, PageQuery pageQuery) {
        return productService.productPage(query, pageQuery);
    }

    @GetMapping("/product/options")
    public R<?> productOptions(ShopProduct query) {
        return R.ok(productService.productList(query));
    }

    @GetMapping("/product/{productId}")
    public R<ShopProduct> getProduct(@PathVariable Long productId) {
        return R.ok(productService.getProduct(productId));
    }

    @PostMapping("/product")
    public R<Void> addProduct(@RequestBody ShopProduct product) {
        productService.saveProduct(product);
        return R.ok();
    }

    @PutMapping("/product")
    public R<Void> editProduct(@RequestBody ShopProduct product) {
        productService.saveProduct(product);
        return R.ok();
    }

    @DeleteMapping("/product/{productIds}")
    public R<Void> removeProduct(@PathVariable Long[] productIds) {
        productService.removeProduct(Arrays.asList(productIds));
        return R.ok();
    }
}
