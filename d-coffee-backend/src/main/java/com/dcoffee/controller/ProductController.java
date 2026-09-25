package com.dcoffee.controller;

import com.dcoffee.common.PageResult;
import com.dcoffee.common.Result;
import com.dcoffee.dto.InventoryAdjustmentRequest;
import com.dcoffee.dto.ProductStatusRequest;
import com.dcoffee.dto.ProductUpsertRequest;
import com.dcoffee.dto.ProductUpdateRequest;
import com.dcoffee.dto.ProductConfigurationRequest;
import com.dcoffee.service.ProductService;
import com.dcoffee.utils.AuthContext;
import com.dcoffee.vo.CategoryView;
import com.dcoffee.vo.ProductView;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/categories")
    public Result<List<CategoryView>> categories() {
        return Result.success(productService.getCategories());
    }

    @GetMapping("/products")
    public Result<PageResult<ProductView>> products(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int pageSize) {
        return Result.success(productService.getPublicProducts(categoryId, keyword, recommended, page, pageSize));
    }

    @GetMapping("/products/{id}")
    public Result<ProductView> product(@PathVariable @Min(1) long id) {
        return Result.success(productService.getPublicProduct(id));
    }

    @GetMapping("/admin/products")
    public Result<PageResult<ProductView>> adminProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int pageSize) {
        return Result.success(productService.getAdminProducts(categoryId, keyword, status, page, pageSize));
    }

    @GetMapping("/admin/products/{id}")
    public Result<ProductView> adminProduct(@PathVariable @Min(1) long id) {
        return Result.success(productService.getAdminProduct(id));
    }

    @PostMapping("/admin/products")
    public Result<Void> create(@Valid @RequestBody ProductUpsertRequest request) {
        productService.create(request);
        return Result.success();
    }

    @PutMapping("/admin/products/{id}")
    public Result<Void> update(@PathVariable @Min(1) long id, @Valid @RequestBody ProductUpdateRequest request) {
        productService.update(id, request);
        return Result.success();
    }

    @PutMapping("/admin/products/{id}/configuration")
    public Result<Void> updateConfiguration(@PathVariable @Min(1) long id,
                                            @RequestBody ProductConfigurationRequest request) {
        productService.updateConfiguration(id, request);
        return Result.success();
    }

    @PatchMapping("/admin/products/{id}/status")
    public Result<Void> updateStatus(@PathVariable @Min(1) long id, @Valid @RequestBody ProductStatusRequest request) {
        productService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @PatchMapping("/admin/products/{id}/inventory")
    public Result<ProductView> adjustInventory(@PathVariable @Min(1) long id,
                                                @Valid @RequestBody InventoryAdjustmentRequest request) {
        return Result.success(productService.adjustInventory(id, request, AuthContext.getRequired().getId()));
    }
}
