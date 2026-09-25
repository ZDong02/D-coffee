package com.dcoffee.service;

import com.dcoffee.dto.CartAddItemRequest;
import com.dcoffee.exception.BizException;
import com.dcoffee.exception.ErrorCode;
import com.dcoffee.mapper.CartMapper;
import com.dcoffee.mapper.ProductMapper;
import com.dcoffee.vo.CartItemView;
import com.dcoffee.vo.CartOptionView;
import com.dcoffee.vo.CartView;
import com.dcoffee.vo.OptionValueView;
import com.dcoffee.vo.ProductExtraView;
import com.dcoffee.vo.ProductOptionView;
import com.dcoffee.vo.ProductView;
import com.dcoffee.vo.StoreView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CartService {
    private static final int MAX_QUANTITY = 99;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CartService(CartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    public CartView getCart(long userId, long storeId) {
        StoreView store = requireStore(storeId);
        Long cartId = cartMapper.findCartId(userId, storeId);
        return buildView(userId, store, cartId);
    }

    @Transactional
    public CartView addItem(long userId, CartAddItemRequest request) {
        StoreView store = requireStore(request.getStoreId());
        ProductView product = productMapper.findProductById(request.getProductId(), true);
        if (product == null || product.getStock() == null || product.getStock() < 1) {
            throw new BizException(ErrorCode.CONFLICT, "商品暂不可购买或库存不足");
        }

        List<Snapshot> snapshots = validateSelections(product, request);
        String signature = signature(snapshots);
        Long cartId = cartMapper.findCartId(userId, store.getId());
        if (cartId == null) {
            cartId = 0L;
            cartMapper.insertCart(userId, store.getId(), cartId);
        }
        Long itemId = cartMapper.findItemId(cartId, product.getId(), signature);
        if (itemId != null) {
            if (cartMapper.addItemQuantity(cartId, itemId) == 0) {
                throw new BizException(ErrorCode.BAD_REQUEST, "商品数量最多为 99");
            }
        } else {
            itemId = cartMapper.findDeletedItemId(cartId, product.getId(), signature);
            if (itemId != null) {
                cartMapper.restoreItem(cartId, itemId);
            } else {
                itemId = 0L;
                cartMapper.insertItem(cartId, product.getId(), signature, 1, itemId);
            }
            for (Snapshot snapshot : snapshots) {
                cartMapper.insertItemOption(itemId, snapshot.selectionType, snapshot.groupName,
                        snapshot.selectedName, snapshot.catalogValueId, snapshot.priceDelta);
            }
        }
        return buildView(userId, store, cartId);
    }

    @Transactional
    public CartView updateQuantity(long userId, long storeId, long itemId, int quantity) {
        StoreView store = requireStore(storeId);
        Long cartId = cartMapper.findCartId(userId, storeId);
        if (cartId == null || cartMapper.updateQuantity(userId, itemId, quantity) == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "购物车商品不存在");
        }
        return buildView(userId, store, cartId);
    }

    @Transactional
    public CartView removeItem(long userId, long storeId, long itemId) {
        StoreView store = requireStore(storeId);
        Long cartId = cartMapper.findCartId(userId, storeId);
        if (cartId == null || cartMapper.softDeleteItem(userId, itemId) == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "购物车商品不存在");
        }
        cartMapper.softDeleteItemOptions(userId, itemId);
        return buildView(userId, store, cartId);
    }

    private List<Snapshot> validateSelections(ProductView product, CartAddItemRequest request) {
        List<ProductOptionView> groups = productMapper.findOptions(product.getId());
        List<ProductExtraView> extras = productMapper.findExtras(product.getId());
        Map<Long, CartAddItemRequest.OptionSelection> selections = new HashMap<>();
        if (request.getOptions() == null || request.getExtraIds() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "商品规格选择格式错误");
        }
        for (CartAddItemRequest.OptionSelection selection : request.getOptions()) {
            if (selection == null || selection.getOptionId() == null || selection.getValueIds() == null
                    || selections.put(selection.getOptionId(), selection) != null) {
                throw new BizException(ErrorCode.BAD_REQUEST, "商品规格选择重复或格式错误");
            }
        }
        List<Snapshot> snapshots = new ArrayList<>();
        for (ProductOptionView group : groups) {
            CartAddItemRequest.OptionSelection selection = selections.remove(group.getId());
            List<Long> valueIds = selection == null ? List.of() : selection.getValueIds();
            Set<Long> uniqueIds = new HashSet<>(valueIds);
            if (uniqueIds.size() != valueIds.size()) {
                throw new BizException(ErrorCode.BAD_REQUEST, "同一规格不能重复选择");
            }
            int selectedCount = valueIds.size();
            int minimum = Boolean.TRUE.equals(group.getRequired())
                    ? Math.max(1, group.getMinSelect()) : group.getMinSelect();
            if (selectedCount < minimum || selectedCount > group.getMaxSelect()) {
                throw new BizException(ErrorCode.BAD_REQUEST, "规格「" + group.getName() + "」选择数量不正确");
            }
            for (Long valueId : valueIds) {
                OptionValueView value = group.getValues().stream()
                        .filter(item -> item.getId().equals(valueId)).findFirst().orElse(null);
                if (value == null) {
                    throw new BizException(ErrorCode.BAD_REQUEST, "商品规格已更新，请刷新后重试");
                }
                snapshots.add(new Snapshot("OPTION", group.getName(), value.getName(), value.getId(),
                        value.getPriceDelta(), group.getId(), value.getId()));
            }
        }
        if (!selections.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "商品规格已更新，请刷新后重试");
        }

        Set<Long> uniqueExtraIds = new HashSet<>(request.getExtraIds());
        if (uniqueExtraIds.size() != request.getExtraIds().size()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "加料不能重复选择");
        }
        for (Long extraId : uniqueExtraIds) {
            ProductExtraView extra = extras.stream().filter(item -> item.getId().equals(extraId)).findFirst().orElse(null);
            if (extra == null || extra.getStock() == null || extra.getStock() <= 0) {
                throw new BizException(ErrorCode.CONFLICT, "所选加料已售罄或不可用，请刷新后重试");
            }
            snapshots.add(new Snapshot("EXTRA", "加料", extra.getName(), extra.getId(), extra.getPrice(),
                    Long.MAX_VALUE, extra.getId()));
        }
        snapshots.sort(Comparator.comparingLong((Snapshot item) -> item.groupOrder)
                .thenComparingLong(item -> item.valueOrder));
        return snapshots;
    }

    private CartView buildView(long userId, StoreView store, Long cartId) {
        List<CartItemView> items = cartId == null ? new ArrayList<>() : cartMapper.findItems(userId, cartId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (CartItemView item : items) {
            List<CartOptionView> options = cartMapper.findItemOptions(userId, item.getId());
            item.setOptions(options);
            BigDecimal unitPrice = item.getBasePrice();
            for (CartOptionView option : options) {
                unitPrice = unitPrice.add(option.getPriceDelta());
            }
            item.setUnitPrice(unitPrice);
            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            totalQuantity += item.getQuantity();
        }
        CartView view = new CartView();
        view.setStoreId(store.getId());
        view.setStoreName(store.getName());
        view.setStoreStatus(store.getStatus());
        view.setItems(items);
        view.setTotalQuantity(totalQuantity);
        view.setTotalAmount(totalAmount);
        return view;
    }

    private StoreView requireStore(long storeId) {
        StoreView store = cartMapper.findStore(storeId);
        if (store == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "门店不存在或暂不可选");
        }
        return store;
    }

    private String signature(List<Snapshot> snapshots) {
        String canonical = snapshots.stream()
                .map(item -> item.selectionType + ":" + item.groupOrder + ":" + item.catalogValueId)
                .reduce((left, right) -> left + "|" + right).orElse("");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK 缺少 SHA-256", exception);
        }
    }

    private static final class Snapshot {
        private final String selectionType;
        private final String groupName;
        private final String selectedName;
        private final Long catalogValueId;
        private final BigDecimal priceDelta;
        private final long groupOrder;
        private final long valueOrder;

        private Snapshot(String selectionType, String groupName, String selectedName, Long catalogValueId,
                         BigDecimal priceDelta, long groupOrder, long valueOrder) {
            this.selectionType = selectionType;
            this.groupName = groupName;
            this.selectedName = selectedName;
            this.catalogValueId = catalogValueId;
            this.priceDelta = priceDelta;
            this.groupOrder = groupOrder;
            this.valueOrder = valueOrder;
        }
    }
}
