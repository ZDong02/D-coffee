package com.dcoffee.mapper;

import com.dcoffee.vo.CartItemView;
import com.dcoffee.vo.CartOptionView;
import com.dcoffee.vo.StoreView;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CartMapper {
    StoreView findStore(@Param("storeId") long storeId);
    Long findCartId(@Param("userId") long userId, @Param("storeId") long storeId);
    Long lockCartId(@Param("userId") long userId, @Param("storeId") long storeId);
    int insertCart(@Param("userId") long userId, @Param("storeId") long storeId,
                   @Param("cartId") Long cartId);
    Long findItemId(@Param("cartId") long cartId, @Param("productId") long productId,
                    @Param("signature") String signature);
    Long findDeletedItemId(@Param("cartId") long cartId, @Param("productId") long productId,
                           @Param("signature") String signature);
    int insertItem(@Param("cartId") long cartId, @Param("productId") long productId,
                   @Param("signature") String signature, @Param("quantity") int quantity,
                   @Param("itemId") Long itemId);
    int addItemQuantity(@Param("cartId") long cartId, @Param("itemId") long itemId);
    int restoreItem(@Param("cartId") long cartId, @Param("itemId") long itemId);
    int insertItemOption(@Param("itemId") long itemId, @Param("selectionType") String selectionType,
                         @Param("groupName") String groupName, @Param("selectedName") String selectedName,
                         @Param("catalogValueId") Long catalogValueId, @Param("priceDelta") BigDecimal priceDelta);
    List<CartItemView> findItems(@Param("userId") long userId, @Param("cartId") long cartId);
    List<CartOptionView> findItemOptions(@Param("userId") long userId, @Param("itemId") long itemId);
    int updateQuantity(@Param("userId") long userId, @Param("itemId") long itemId,
                       @Param("quantity") int quantity);
    int softDeleteItem(@Param("userId") long userId, @Param("itemId") long itemId);
    int softDeleteItemOptions(@Param("userId") long userId, @Param("itemId") long itemId);
}
