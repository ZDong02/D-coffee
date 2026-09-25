package com.dcoffee.service;

import com.dcoffee.dto.CategoryUpsertRequest;
import com.dcoffee.exception.BizException;
import com.dcoffee.exception.ErrorCode;
import com.dcoffee.mapper.CategoryMapper;
import com.dcoffee.vo.CategoryView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class CategoryService {
    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,48}");
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) { this.categoryMapper = categoryMapper; }

    public List<CategoryView> list() { return categoryMapper.findAdminCategories(); }

    @Transactional
    public void create(CategoryUpsertRequest request) {
        normalizeAndValidate(request);
        if (categoryMapper.codeExists(request.getCode(), null)) {
            throw new BizException(ErrorCode.CONFLICT, "分类编码已存在");
        }
        categoryMapper.insertCategory(request);
    }

    @Transactional
    public void update(long id, CategoryUpsertRequest request) {
        normalizeAndValidate(request);
        if (!categoryMapper.categoryExists(id)) throw new BizException(ErrorCode.NOT_FOUND, "分类不存在");
        if (categoryMapper.codeExists(request.getCode(), id)) {
            throw new BizException(ErrorCode.CONFLICT, "分类编码已被其他分类使用");
        }
        categoryMapper.updateCategory(id, request);
    }

    @Transactional
    public void updateStatus(long id, String status) {
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "分类状态无效");
        }
        if (!categoryMapper.categoryExists(id)) throw new BizException(ErrorCode.NOT_FOUND, "分类不存在");
        categoryMapper.updateStatus(id, status);
    }

    private void normalizeAndValidate(CategoryUpsertRequest request) {
        if (request == null || request.getCode() == null || request.getName() == null || request.getSort() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "分类信息不完整");
        }
        request.setCode(request.getCode().trim());
        request.setName(request.getName().trim());
        if (!CODE_PATTERN.matcher(request.getCode()).matches() || request.getName().isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "分类编码或名称无效");
        }
        if (request.getIconUrl() != null) request.setIconUrl(request.getIconUrl().trim());
        if (request.getIconUrl() != null && request.getIconUrl().isEmpty()) request.setIconUrl(null);
    }
}
