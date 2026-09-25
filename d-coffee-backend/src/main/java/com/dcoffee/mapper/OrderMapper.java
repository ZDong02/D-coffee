package com.dcoffee.mapper;

import com.dcoffee.model.OrderItemRecord;
import com.dcoffee.model.OrderRecord;
import com.dcoffee.vo.CartOptionView;
import com.dcoffee.vo.OrderStockReservationView;
import com.dcoffee.vo.OrderView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int insertOrder(@Param("order") OrderRecord order);
    int insertItem(@Param("item") OrderItemRecord item);
    int insertItemOption(@Param("itemId") long itemId, @Param("selectionType") String selectionType,
                         @Param("groupName") String groupName, @Param("selectedName") String selectedName,
                         @Param("priceDelta") java.math.BigDecimal priceDelta);
    int insertReservation(@Param("orderId") long orderId, @Param("itemId") long itemId,
                          @Param("type") String type, @Param("productId") Long productId,
                          @Param("extraId") Long extraId, @Param("quantity") int quantity);
    int clearCart(@Param("userId") long userId, @Param("cartId") long cartId);
    List<OrderView> findOrders(@Param("userId") long userId);
    OrderView findOrder(@Param("userId") long userId, @Param("orderId") long orderId);
    List<OrderView.OrderItemView> findItems(@Param("orderId") long orderId);
    List<CartOptionView> findItemOptions(@Param("orderItemId") long orderItemId);
    OrderView lockOrder(@Param("userId") long userId, @Param("orderId") long orderId);
    List<OrderStockReservationView> findReservations(@Param("orderId") long orderId);
    int markReservationsReleased(@Param("orderId") long orderId);
    int cancelOrder(@Param("userId") long userId, @Param("orderId") long orderId);
}
