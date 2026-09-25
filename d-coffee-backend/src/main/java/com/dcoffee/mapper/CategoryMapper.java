package com.dcoffee.mapper;

import com.dcoffee.dto.CategoryUpsertRequest;
import com.dcoffee.vo.CategoryView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CategoryMapper {
    List<CategoryView> findAdminCategories();
    boolean categoryExists(@Param("id") long id);
    boolean codeExists(@Param("code") String code, @Param("excludeId") Long excludeId);
    int insertCategory(@Param("category") CategoryUpsertRequest category);
    int updateCategory(@Param("id") long id, @Param("category") CategoryUpsertRequest category);
    int updateStatus(@Param("id") long id, @Param("status") String status);
}
