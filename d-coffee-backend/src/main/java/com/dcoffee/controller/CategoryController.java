package com.dcoffee.controller;

import com.dcoffee.common.Result;
import com.dcoffee.dto.CategoryStatusRequest;
import com.dcoffee.dto.CategoryUpsertRequest;
import com.dcoffee.service.CategoryService;
import com.dcoffee.vo.CategoryView;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) { this.categoryService = categoryService; }

    @GetMapping("/admin/categories")
    public Result<List<CategoryView>> list() { return Result.success(categoryService.list()); }

    @PostMapping("/admin/categories")
    public Result<Void> create(@Valid @RequestBody CategoryUpsertRequest request) {
        categoryService.create(request);
        return Result.success();
    }

    @PutMapping("/admin/categories/{id}")
    public Result<Void> update(@PathVariable @Min(1) long id, @Valid @RequestBody CategoryUpsertRequest request) {
        categoryService.update(id, request);
        return Result.success();
    }

    @PatchMapping("/admin/categories/{id}/status")
    public Result<Void> updateStatus(@PathVariable @Min(1) long id, @Valid @RequestBody CategoryStatusRequest request) {
        categoryService.updateStatus(id, request.getStatus());
        return Result.success();
    }
}
