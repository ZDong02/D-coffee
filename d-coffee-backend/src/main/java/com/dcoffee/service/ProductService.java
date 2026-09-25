package com.dcoffee.service;

import com.dcoffee.common.PageResult;
import com.dcoffee.dto.InventoryAdjustmentRequest;
import com.dcoffee.dto.ProductUpsertRequest;
import com.dcoffee.dto.ProductUpdateRequest;
import com.dcoffee.dto.ProductConfigurationRequest;
import com.dcoffee.exception.BizException;
import com.dcoffee.exception.ErrorCode;
import com.dcoffee.mapper.ProductMapper;
import com.dcoffee.vo.CategoryView;
import com.dcoffee.vo.ProductView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ProductService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final Pattern CONFIG_CODE_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,48}");
    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<CategoryView> getCategories() {
        return productMapper.findActiveCategories();
    }

    public PageResult<ProductView> getPublicProducts(Long categoryId, String keyword, Boolean recommended,
                                                       int page, int pageSize) {
        return getProducts(categoryId, keyword, recommended, "ON_SALE", page, pageSize);
    }

    public PageResult<ProductView> getAdminProducts(Long categoryId, String keyword, String status,
                                                      int page, int pageSize) {
        if (status != null && !List.of("DRAFT", "ON_SALE", "OFF_SALE").contains(status)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "商品状态无效");
        }
        return getProducts(categoryId, keyword, null, status == null ? "ALL" : status, page, pageSize);
    }

    public ProductView getPublicProduct(long id) {
        return getProduct(id, true);
    }

    public ProductView getAdminProduct(long id) {
        return getProduct(id, false);
    }

    @Transactional
    public void create(ProductUpsertRequest request) {
        validate(request);
        if (!productMapper.categoryExists(request.getCategoryId())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "商品分类不存在或已停用");
        }
        productMapper.insertProduct(request);
    }

    @Transactional
    public void update(long id, ProductUpdateRequest updateRequest) {
        ProductUpsertRequest request = updateRequest.toProductRequest();
        validate(request);
        if (!productMapper.categoryExists(request.getCategoryId())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "商品分类不存在或已停用");
        }
        if (!productMapper.productExists(id)) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        productMapper.updateProduct(id, request);
    }

    @Transactional
    public void updateConfiguration(long productId, ProductConfigurationRequest request) {
        if (!productMapper.productExists(productId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        validateConfiguration(request);

        productMapper.deactivateOptionValues(productId);
        productMapper.deactivateOptionGroups(productId);
        productMapper.deactivateExtras(productId);
        for (ProductConfigurationRequest.OptionGroup option : request.getOptions()) {
            productMapper.upsertOptionGroup(productId, option);
            Long optionId = productMapper.findOptionId(productId, option.getCode());
            if (optionId == null) {
                throw new BizException(ErrorCode.INTERNAL_ERROR, "规格保存失败");
            }
            for (ProductConfigurationRequest.OptionValue value : option.getValues()) {
                productMapper.upsertOptionValue(optionId, value);
            }
        }
        for (ProductConfigurationRequest.Extra extra : request.getExtras()) {
            productMapper.upsertExtra(productId, extra);
        }
    }

    @Transactional
    public void updateStatus(long id, String status) {
        if (!List.of("DRAFT", "ON_SALE", "OFF_SALE").contains(status)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "商品状态无效");
        }
        if (!productMapper.productExists(id)) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        productMapper.updateStatus(id, status);
    }

    @Transactional
    public ProductView adjustInventory(long id, InventoryAdjustmentRequest request, long adminId) {
        int delta = request.getDelta();
        if (delta == 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "库存调整数量不能为 0");
        }
        Integer beforeStock = productMapper.getStock(id);
        if (beforeStock == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        long after = (long) beforeStock + delta;
        if (after < 0 || after > Integer.MAX_VALUE) {
            throw new BizException(ErrorCode.BAD_REQUEST, "库存调整后数量无效");
        }
        if (productMapper.adjustStock(id, delta) == 0) {
            throw new BizException(ErrorCode.CONFLICT, "库存更新失败，请重试");
        }
        productMapper.insertInventoryLog(id, adminId, delta, beforeStock, (int) after, request.getReason());
        return getProduct(id, false);
    }

    private PageResult<ProductView> getProducts(Long categoryId, String keyword, Boolean recommended,
                                                 String status, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(MAX_PAGE_SIZE, Math.max(1, pageSize));
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        long offset = (long) (safePage - 1) * safePageSize;
        List<ProductView> records = productMapper.findProducts(categoryId, normalizedKeyword, recommended,
                status, offset, safePageSize);
        return new PageResult<>(records, productMapper.countProducts(categoryId, normalizedKeyword, recommended,
                status), safePage, safePageSize);
    }

    private ProductView getProduct(long id, boolean publicOnly) {
        ProductView product = productMapper.findProductById(id, publicOnly);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        product.setOptions(productMapper.findOptions(id));
        product.setExtras(productMapper.findExtras(id));
        return product;
    }

    private void validate(ProductUpsertRequest request) {
        if (request.getStock() < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "库存不能小于 0");
        }
        BigDecimal originalPrice = request.getOriginalPrice();
        if (originalPrice != null && originalPrice.compareTo(request.getPrice()) < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "划线价不能低于售价");
        }
    }

    private void validateConfiguration(ProductConfigurationRequest request) {
        if (request == null || request.getOptions() == null || request.getExtras() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "规格和加料列表不能为空");
        }
        if (request.getOptions().size() > 20 || request.getExtras().size() > 50) {
            throw new BizException(ErrorCode.BAD_REQUEST, "规格组最多 20 个，加料最多 50 个");
        }
        Set<String> optionCodes = new HashSet<>();
        for (ProductConfigurationRequest.OptionGroup option : request.getOptions()) {
            if (option == null || !validCode(option.getCode())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "规格组编码无效或重复");
            }
            option.setCode(option.getCode().trim());
            if (!optionCodes.add(option.getCode())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "规格组编码无效或重复");
            }
            if (option.getName() == null || option.getName().isBlank() || option.getName().trim().length() > 64) {
                throw new BizException(ErrorCode.BAD_REQUEST, "规格组名称不能为空且不能超过 64 个字符");
            }
            option.setName(option.getName().trim());
            if (option.getSelectionType() == null || !List.of("SINGLE", "MULTIPLE").contains(option.getSelectionType())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "规格组选择类型无效");
            }
            if (option.getRequired() == null || option.getMinSelect() == null || option.getMaxSelect() == null
                    || option.getSort() == null || option.getValues() == null || option.getValues().isEmpty()) {
                throw new BizException(ErrorCode.BAD_REQUEST, "规格组必须包含有效的规格值和选择规则");
            }
            int min = option.getMinSelect();
            int max = option.getMaxSelect();
            if (min < 0 || max < 1 || min > max || max > option.getValues().size()
                    || max > 20 || (option.getRequired() && min < 1)
                    || ("SINGLE".equals(option.getSelectionType()) && max != 1)) {
                throw new BizException(ErrorCode.BAD_REQUEST, "规格组最少/最多选择数量不正确");
            }
            Set<String> valueCodes = new HashSet<>();
            for (ProductConfigurationRequest.OptionValue value : option.getValues()) {
                if (value == null || !validCode(value.getCode())) {
                    throw new BizException(ErrorCode.BAD_REQUEST, "规格值编码无效或重复");
                }
                value.setCode(value.getCode().trim());
                if (!valueCodes.add(value.getCode())) {
                    throw new BizException(ErrorCode.BAD_REQUEST, "规格值编码无效或重复");
                }
                if (value.getName() == null || value.getName().isBlank() || value.getName().trim().length() > 64
                        || value.getPriceDelta() == null || value.getPriceDelta().signum() < 0
                        || value.getPriceDelta().precision() > 10 || value.getPriceDelta().scale() > 2
                        || value.getSort() == null) {
                    throw new BizException(ErrorCode.BAD_REQUEST, "规格值名称、加价或排序无效");
                }
                value.setName(value.getName().trim());
            }
        }

        Set<String> extraCodes = new HashSet<>();
        for (ProductConfigurationRequest.Extra extra : request.getExtras()) {
            if (extra == null || !validCode(extra.getCode())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "加料编码无效或重复");
            }
            extra.setCode(extra.getCode().trim());
            if (!extraCodes.add(extra.getCode())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "加料编码无效或重复");
            }
            if (extra.getName() == null || extra.getName().isBlank() || extra.getName().trim().length() > 64
                    || extra.getPrice() == null || extra.getPrice().signum() < 0 || extra.getPrice().precision() > 10
                    || extra.getPrice().scale() > 2 || extra.getStock() == null || extra.getStock() < 0
                    || extra.getSort() == null) {
                throw new BizException(ErrorCode.BAD_REQUEST, "加料名称、价格、库存或排序无效");
            }
            extra.setName(extra.getName().trim());
        }
    }

    private boolean validCode(String code) {
        return code != null && CONFIG_CODE_PATTERN.matcher(code.trim()).matches();
    }
}
