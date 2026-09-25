package com.dcoffee.mapper;

import com.dcoffee.dto.ProductUpsertRequest;
import com.dcoffee.dto.ProductConfigurationRequest;
import com.dcoffee.vo.CategoryView;
import com.dcoffee.vo.ProductExtraView;
import com.dcoffee.vo.ProductOptionView;
import com.dcoffee.vo.ProductView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    List<CategoryView> findActiveCategories();
    List<ProductView> findProducts(@Param("categoryId") Long categoryId, @Param("keyword") String keyword,
                                   @Param("recommended") Boolean recommended, @Param("status") String status,
                                   @Param("offset") long offset, @Param("pageSize") int pageSize);
    long countProducts(@Param("categoryId") Long categoryId, @Param("keyword") String keyword,
                       @Param("recommended") Boolean recommended, @Param("status") String status);
    ProductView findProductById(@Param("id") long id, @Param("publicOnly") boolean publicOnly);
    List<ProductOptionView> findOptions(@Param("productId") long productId);
    List<ProductExtraView> findExtras(@Param("productId") long productId);
    boolean categoryExists(@Param("categoryId") long categoryId);
    boolean productExists(@Param("id") long id);
    Integer getStock(@Param("id") long id);
    int insertProduct(@Param("product") ProductUpsertRequest product);
    int updateProduct(@Param("id") long id, @Param("product") ProductUpsertRequest product);
    int updateStatus(@Param("id") long id, @Param("status") String status);
    int adjustStock(@Param("id") long id, @Param("delta") int delta);
    int reserveExtraStock(@Param("id") long id, @Param("quantity") int quantity);
    int releaseExtraStock(@Param("id") long id, @Param("quantity") int quantity);
    int insertInventoryLog(@Param("productId") long productId, @Param("adminId") long adminId,
                           @Param("delta") int delta, @Param("beforeStock") int beforeStock,
                           @Param("afterStock") int afterStock, @Param("reason") String reason);
    int deactivateOptionValues(@Param("productId") long productId);
    int deactivateOptionGroups(@Param("productId") long productId);
    int deactivateExtras(@Param("productId") long productId);
    int upsertOptionGroup(@Param("productId") long productId,
                          @Param("option") ProductConfigurationRequest.OptionGroup option);
    Long findOptionId(@Param("productId") long productId, @Param("code") String code);
    int upsertOptionValue(@Param("optionId") long optionId,
                          @Param("value") ProductConfigurationRequest.OptionValue value);
    int upsertExtra(@Param("productId") long productId, @Param("extra") ProductConfigurationRequest.Extra extra);
}
