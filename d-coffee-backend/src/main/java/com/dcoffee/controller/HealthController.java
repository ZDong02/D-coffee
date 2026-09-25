package com.dcoffee.controller;

import com.dcoffee.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 用于本地部署检查的服务存活接口，不属于业务接口。 */
@RestController
public class HealthController {
    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.success(Map.of("status", "UP", "service", "d-coffee-backend"));
    }
}
