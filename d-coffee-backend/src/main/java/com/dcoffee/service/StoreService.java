package com.dcoffee.service;

import com.dcoffee.mapper.StoreMapper;
import com.dcoffee.vo.StoreView;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreService {
    private final StoreMapper storeMapper;

    public StoreService(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    public List<StoreView> getAvailableStores() {
        return storeMapper.findAvailableStores();
    }
}
