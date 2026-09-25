package com.dcoffee.service;

import com.dcoffee.dto.OrderCreateRequest;
import com.dcoffee.exception.BizException;
import com.dcoffee.exception.ErrorCode;
import com.dcoffee.mapper.CartMapper;
import com.dcoffee.mapper.OrderMapper;
import com.dcoffee.mapper.ProductMapper;
import com.dcoffee.model.OrderItemRecord;
import com.dcoffee.model.OrderRecord;
import com.dcoffee.vo.CartItemView;
import com.dcoffee.vo.CartOptionView;
import com.dcoffee.vo.OrderStockReservationView;
import com.dcoffee.vo.OrderView;
import com.dcoffee.vo.OptionValueView;
import com.dcoffee.vo.ProductExtraView;
import com.dcoffee.vo.ProductOptionView;
import com.dcoffee.vo.ProductView;
import com.dcoffee.vo.StoreView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    public OrderService(CartMapper cartMapper, ProductMapper productMapper, OrderMapper orderMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderView create(long userId, OrderCreateRequest request) {
        StoreView store = cartMapper.findStore(request.getStoreId());
        if (store == null) throw new BizException(ErrorCode.NOT_FOUND, "门店不存在或暂不可选");
        if (!"OPEN".equals(store.getStatus())) throw new BizException(ErrorCode.CONFLICT, "门店当前未营业，暂不能提交订单");
        Long cartId = cartMapper.lockCartId(userId, store.getId());
        if (cartId == null) throw new BizException(ErrorCode.BAD_REQUEST, "购物车为空");
        List<CartItemView> cartItems = cartMapper.findItems(userId, cartId);
        if (cartItems == null || cartItems.isEmpty()) throw new BizException(ErrorCode.BAD_REQUEST, "购物车为空");

        List<PreparedItem> preparedItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemView cartItem : cartItems) {
            if (cartItem.getQuantity() == null || cartItem.getQuantity() < 1 || cartItem.getQuantity() > 99) {
                throw new BizException(ErrorCode.CONFLICT, "购物车商品数量无效，请刷新后重试");
            }
            ProductView product = productMapper.findProductById(cartItem.getProductId(), true);
            if (product == null) throw new BizException(ErrorCode.CONFLICT, "商品已下架，请移除后重试");
            List<CartOptionView> storedOptions = cartMapper.findItemOptions(userId, cartItem.getId());
            List<ResolvedOption> options = resolveOptions(product, storedOptions);
            BigDecimal unitPrice = product.getPrice();
            for (ResolvedOption option : options) unitPrice = unitPrice.add(option.priceDelta);
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subtotal);
            preparedItems.add(new PreparedItem(cartItem, product, options, unitPrice, subtotal));
        }

        OrderRecord order = new OrderRecord();
        order.setOrderNo("DC" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setStoreId(store.getId());
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setRemark(request.getRemark() == null ? "" : request.getRemark().trim());
        if (orderMapper.insertOrder(order) != 1 || order.getId() == null) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "订单创建失败");
        }

        for (PreparedItem prepared : preparedItems) {
            CartItemView cartItem = prepared.cartItem;
            ProductView product = prepared.product;
            int quantity = cartItem.getQuantity();
            if (productMapper.adjustStock(product.getId(), -quantity) != 1) {
                throw new BizException(ErrorCode.CONFLICT, "商品「" + product.getName() + "」库存不足");
            }
            OrderItemRecord item = new OrderItemRecord();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setImageUrl(product.getImageUrl());
            item.setBasePrice(product.getPrice());
            item.setUnitPrice(prepared.unitPrice);
            item.setQuantity(quantity);
            item.setSubtotal(prepared.subtotal);
            if (orderMapper.insertItem(item) != 1 || item.getId() == null) {
                throw new BizException(ErrorCode.INTERNAL_ERROR, "订单商品保存失败");
            }
            orderMapper.insertReservation(order.getId(), item.getId(), "PRODUCT", product.getId(), null, quantity);
            for (ResolvedOption option : prepared.options) {
                orderMapper.insertItemOption(item.getId(), option.type, option.groupName, option.selectedName, option.priceDelta);
                if ("EXTRA".equals(option.type)) {
                    int reservedQuantity = quantity;
                    if (productMapper.reserveExtraStock(option.extraId, reservedQuantity) != 1) {
                        throw new BizException(ErrorCode.CONFLICT, "加料「" + option.selectedName + "」库存不足");
                    }
                    orderMapper.insertReservation(order.getId(), item.getId(), "EXTRA", null,
                            option.extraId, reservedQuantity);
                }
            }
        }
        orderMapper.clearCart(userId, cartId);
        return get(userId, order.getId());
    }

    public List<OrderView> list(long userId) {
        List<OrderView> orders = orderMapper.findOrders(userId);
        for (OrderView order : orders) attachItems(order);
        return orders;
    }

    public OrderView get(long userId, long orderId) {
        OrderView order = orderMapper.findOrder(userId, orderId);
        if (order == null) throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        attachItems(order);
        return order;
    }

    @Transactional
    public OrderView cancel(long userId, long orderId) {
        OrderView locked = orderMapper.lockOrder(userId, orderId);
        if (locked == null) throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        if (!"PENDING_PAYMENT".equals(locked.getStatus()) || !"UNPAID".equals(locked.getPaymentStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "只有未支付订单可以取消");
        }
        List<OrderStockReservationView> reservations = orderMapper.findReservations(orderId);
        for (OrderStockReservationView reservation : reservations) {
            int updated = "PRODUCT".equals(reservation.getReservationType())
                    ? productMapper.adjustStock(reservation.getProductId(), reservation.getQuantity())
                    : productMapper.releaseExtraStock(reservation.getExtraId(), reservation.getQuantity());
            if (updated != 1) throw new BizException(ErrorCode.CONFLICT, "库存回补失败，请稍后重试");
        }
        orderMapper.markReservationsReleased(orderId);
        if (orderMapper.cancelOrder(userId, orderId) != 1) throw new BizException(ErrorCode.CONFLICT, "订单状态已变化，请刷新");
        return get(userId, orderId);
    }

    private void attachItems(OrderView order) {
        List<OrderView.OrderItemView> items = orderMapper.findItems(order.getId());
        for (OrderView.OrderItemView item : items) item.setOptions(orderMapper.findItemOptions(item.getId()));
        order.setItems(items);
    }

    private List<ResolvedOption> resolveOptions(ProductView product, List<CartOptionView> stored) {
        List<ProductOptionView> groups = productMapper.findOptions(product.getId());
        List<ProductExtraView> extras = productMapper.findExtras(product.getId());
        Map<String, Integer> groupCounts = new HashMap<>();
        Set<Long> selectedExtraIds = new HashSet<>();
        List<ResolvedOption> resolved = new ArrayList<>();
        for (CartOptionView option : stored) {
            if ("OPTION".equals(option.getSelectionType())) {
                ProductOptionView group = groups.stream().filter(value -> value.getName().equals(option.getGroupName())).findFirst()
                        .orElseThrow(() -> new BizException(ErrorCode.CONFLICT, "商品规格已更新，请刷新购物车"));
                OptionValueView value = group.getValues().stream().filter(item -> item.getName().equals(option.getSelectedName())).findFirst()
                        .orElseThrow(() -> new BizException(ErrorCode.CONFLICT, "商品规格已更新，请刷新购物车"));
                groupCounts.merge(group.getId().toString(), 1, Integer::sum);
                resolved.add(new ResolvedOption("OPTION", group.getName(), value.getName(), value.getPriceDelta(), null));
            } else if ("EXTRA".equals(option.getSelectionType())) {
                ProductExtraView extra = extras.stream().filter(value -> value.getName().equals(option.getSelectedName())).findFirst()
                        .orElseThrow(() -> new BizException(ErrorCode.CONFLICT, "所选加料已下架，请刷新购物车"));
                if (extra.getStock() == null || extra.getStock() < 1 || !selectedExtraIds.add(extra.getId())) {
                    throw new BizException(ErrorCode.CONFLICT, "所选加料已售罄或重复，请刷新购物车");
                }
                resolved.add(new ResolvedOption("EXTRA", "加料", extra.getName(), extra.getPrice(), extra.getId()));
            } else {
                throw new BizException(ErrorCode.CONFLICT, "购物车规格格式错误，请刷新购物车");
            }
        }
        for (ProductOptionView group : groups) {
            int count = groupCounts.getOrDefault(group.getId().toString(), 0);
            int minimum = Boolean.TRUE.equals(group.getRequired()) ? Math.max(1, group.getMinSelect()) : group.getMinSelect();
            if (count < minimum || count > group.getMaxSelect()) {
                throw new BizException(ErrorCode.CONFLICT, "商品规格已更新，请重新选择");
            }
        }
        return resolved;
    }

    private static final class PreparedItem {
        private final CartItemView cartItem;
        private final ProductView product;
        private final List<ResolvedOption> options;
        private final BigDecimal unitPrice;
        private final BigDecimal subtotal;
        private PreparedItem(CartItemView cartItem, ProductView product, List<ResolvedOption> options,
                             BigDecimal unitPrice, BigDecimal subtotal) {
            this.cartItem = cartItem; this.product = product; this.options = options;
            this.unitPrice = unitPrice; this.subtotal = subtotal;
        }
    }

    private static final class ResolvedOption {
        private final String type;
        private final String groupName;
        private final String selectedName;
        private final BigDecimal priceDelta;
        private final Long extraId;
        private ResolvedOption(String type, String groupName, String selectedName, BigDecimal priceDelta, Long extraId) {
            this.type = type; this.groupName = groupName; this.selectedName = selectedName;
            this.priceDelta = priceDelta; this.extraId = extraId;
        }
    }
}
