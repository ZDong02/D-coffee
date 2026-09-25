package com.dcoffee.mapper;

import com.dcoffee.vo.StoreView;

import java.util.List;

public interface StoreMapper {
    List<StoreView> findAvailableStores();
}
