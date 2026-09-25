package com.dcoffee.controller;

import com.dcoffee.common.Result;
import com.dcoffee.service.StoreService;
import com.dcoffee.vo.StoreView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/stores")
    public Result<List<StoreView>> stores() {
        return Result.success(storeService.getAvailableStores());
    }
}
