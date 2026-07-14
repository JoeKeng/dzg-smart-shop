package com.dzg.shop.controller;

import com.dzg.common.core.domain.R;
import com.dzg.common.mybatis.core.page.PageQuery;
import com.dzg.common.mybatis.core.page.TableDataInfo;
import com.dzg.shop.domain.ShopCustomer;
import com.dzg.shop.service.ShopCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RequiredArgsConstructor
@RestController
public class ShopCustomerController {

    private final ShopCustomerService customerService;

    @GetMapping("/customer/list")
    public TableDataInfo<ShopCustomer> customerPage(ShopCustomer query, PageQuery pageQuery) {
        return customerService.customerPage(query, pageQuery);
    }

    @GetMapping("/customer/options")
    public R<?> customerOptions(ShopCustomer query) {
        return R.ok(customerService.customerList(query));
    }

    @GetMapping("/customer/{customerId}")
    public R<ShopCustomer> getCustomer(@PathVariable Long customerId) {
        return R.ok(customerService.getCustomer(customerId));
    }

    @PostMapping("/customer")
    public R<Void> addCustomer(@RequestBody ShopCustomer customer) {
        customerService.saveCustomer(customer);
        return R.ok();
    }

    @PutMapping("/customer")
    public R<Void> editCustomer(@RequestBody ShopCustomer customer) {
        customerService.saveCustomer(customer);
        return R.ok();
    }

    @DeleteMapping("/customer/{customerIds}")
    public R<Void> removeCustomer(@PathVariable Long[] customerIds) {
        customerService.removeCustomer(Arrays.asList(customerIds));
        return R.ok();
    }
}
