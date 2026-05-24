package com.example.ecommerce.ecommerce_backend.application.service.admin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.AttributeGroupEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.BrandEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.AttributeGroupJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.AttributeJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.BrandJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CategoryJpaRepository;
import com.example.ecommerce.ecommerce_backend.shared.util.CatalogSlugUtil;

@Service
public class AdminCatalogManagementService {

    private static final Logger log = LoggerFactory.getLogger(AdminCatalogManagementService.class);

    private final CategoryJpaRepository categoryRepo;
    private final BrandJpaRepository brandRepo;
    private final AttributeGroupJpaRepository attributeGroupRepo;
    private final AttributeJpaRepository attributeRepo;

    public AdminCatalogManagementService(
            CategoryJpaRepository categoryRepo,
            BrandJpaRepository brandRepo,
            AttributeGroupJpaRepository attributeGroupRepo,
            AttributeJpaRepository attributeRepo
    ) {
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.attributeGroupRepo = attributeGroupRepo;
        this.attributeRepo = attributeRepo;
    }

    // ============ CATEGORIES ============

    public Page<CategoryEntity> listCategories(Pageable pageable) {
        return categoryRepo.findAll(pageable);
    }

    @Transactional
    public CategoryEntity createCategory(String name, Long parentId, Integer sortOrder) {
        log.info("Creating category: name={}, parentId={}, sortOrder={}", name, parentId, sortOrder);
        
        // Validate parent if provided
        if (parentId != null) {
            categoryRepo.findById(parentId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Parent category not found"));
        }

        // Generate slug
        String baseSlug = CatalogSlugUtil.slugify(name);
        
        // Check for duplicate slug
        String slug = categoryRepo.existsBySlug(baseSlug) 
            ? baseSlug + "-" + System.currentTimeMillis()
            : baseSlug;

        // Build path
        final String finalSlug = slug;
        String path = parentId != null 
            ? categoryRepo.findById(parentId)
                .map(c -> c.getPath() + "/" + finalSlug)
                .orElse("/" + finalSlug)
            : "/" + finalSlug;

        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        category.setSlug(finalSlug);
        category.setPath(path);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);
        category.setActive(true);

        return categoryRepo.save(category);
    }

    @Transactional
    public CategoryEntity updateCategory(Long id, String name, Long parentId, Integer sortOrder, Boolean active) {
        log.info("Updating category: id={}, name={}, parentId={}, sortOrder={}, active={}", 
            id, name, parentId, sortOrder, active);
        
        CategoryEntity category = categoryRepo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Category not found"));

        // Validate parent if changed
        if (parentId != null && !parentId.equals(category.getParentId())) {
            categoryRepo.findById(parentId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Parent category not found"));
            
            // Update path
            String slug = category.getSlug();
            String newPath = categoryRepo.findById(parentId)
                .map(c -> c.getPath() + "/" + slug)
                .orElse("/" + slug);
            category.setPath(newPath);
        }

        category.setName(name);
        if (parentId != null) {
            category.setParentId(parentId);
        }
        if (sortOrder != null) {
            category.setSortOrder(sortOrder);
        }
        if (active != null) {
            category.setActive(active);
        }

        return categoryRepo.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category: id={}", id);
        
        CategoryEntity category = categoryRepo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Category not found"));

        // Check if category has children
        if (categoryRepo.existsByParentId(id)) {
            throw BusinessException.businessRule(ErrorCode.BAD_REQUEST, 
                "Cannot delete category with child categories");
        }

        // Soft delete by setting inactive
        category.setActive(false);
        categoryRepo.save(category);
    }

    // ============ BRANDS ============

    public Page<BrandEntity> listBrands(Pageable pageable) {
        return brandRepo.findAll(pageable);
    }

    @Transactional
    public BrandEntity createBrand(String name, String logoUrl) {
        log.info("Creating brand: name={}, logoUrl={}", name, logoUrl);
        
        // Generate slug
        String baseSlug = CatalogSlugUtil.slugify(name);
        String slug = brandRepo.existsBySlug(baseSlug) 
            ? baseSlug + "-" + System.currentTimeMillis()
            : baseSlug;

        BrandEntity brand = new BrandEntity();
        brand.setName(name);
        brand.setSlug(slug);
        brand.setLogoUrl(logoUrl);
        brand.setActive(true);

        return brandRepo.save(brand);
    }

    @Transactional
    public BrandEntity updateBrand(Long id, String name, String logoUrl, Boolean active) {
        log.info("Updating brand: id={}, name={}, logoUrl={}, active={}", id, name, logoUrl, active);
        
        BrandEntity brand = brandRepo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Brand not found"));

        brand.setName(name);
        if (logoUrl != null) {
            brand.setLogoUrl(logoUrl);
        }
        if (active != null) {
            brand.setActive(active);
        }

        return brandRepo.save(brand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        log.info("Deleting brand: id={}", id);
        
        BrandEntity brand = brandRepo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Brand not found"));

        // Soft delete by setting inactive
        brand.setActive(false);
        brandRepo.save(brand);
    }

    // ============ ATTRIBUTE GROUPS ============

    public List<AttributeGroupEntity> listAttributeGroups() {
        return attributeGroupRepo.findAll();
    }

    @Transactional
    public AttributeGroupEntity createAttributeGroup(String name, String description, Integer sortOrder) {
        log.info("Creating attribute group: name={}, description={}, sortOrder={}", name, description, sortOrder);
        
        AttributeGroupEntity group = new AttributeGroupEntity();
        group.setName(name);
        group.setDescription(description);
        group.setSortOrder(sortOrder != null ? sortOrder : 0);
        group.setIsActive(true);

        return attributeGroupRepo.save(group);
    }

    @Transactional
    public AttributeGroupEntity updateAttributeGroup(Long id, String name, String description, Integer sortOrder) {
        log.info("Updating attribute group: id={}, name={}, description={}, sortOrder={}", 
            id, name, description, sortOrder);
        
        AttributeGroupEntity group = attributeGroupRepo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Attribute group not found"));

        group.setName(name);
        if (description != null) {
            group.setDescription(description);
        }
        if (sortOrder != null) {
            group.setSortOrder(sortOrder);
        }

        return attributeGroupRepo.save(group);
    }

    @Transactional
    public void deleteAttributeGroup(Long id) {
        log.info("Deleting attribute group: id={}", id);
        
        AttributeGroupEntity group = attributeGroupRepo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Attribute group not found"));

        // Check if group has attributes
        if (attributeRepo.existsByAttributeGroupId(id)) {
            throw BusinessException.businessRule(ErrorCode.BAD_REQUEST, 
                "Cannot delete attribute group with attributes");
        }

        attributeGroupRepo.delete(group);
    }

    // ============ ATTRIBUTES ============

    public Page<AttributeEntity> listAttributes(Long groupId, Pageable pageable) {
        if (groupId != null) {
            return attributeRepo.findByAttributeGroupId(groupId, pageable);
        }
        return attributeRepo.findAll(pageable);
    }

    @Transactional
    public AttributeEntity createAttribute(
            Long attributeGroupId,
            String name,
            AttributeEntity.DataType dataType,
            String unit,
            String description,
            Integer sortOrder,
            Boolean isFilterable,
            Boolean isComparable
    ) {
        log.info("Creating attribute: groupId={}, name={}, dataType={}", attributeGroupId, name, dataType);
        
        // Validate attribute group
        attributeGroupRepo.findById(attributeGroupId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Attribute group not found"));

        // Generate slug
        String baseSlug = CatalogSlugUtil.slugify(name);
        String slug = attributeRepo.existsBySlug(baseSlug) 
            ? baseSlug + "-" + System.currentTimeMillis()
            : baseSlug;

        AttributeEntity attribute = new AttributeEntity();
        attribute.setAttributeGroupId(attributeGroupId);
        attribute.setName(name);
        attribute.setSlug(slug);
        attribute.setDataType(dataType != null ? dataType : AttributeEntity.DataType.TEXT);
        attribute.setUnit(unit);
        attribute.setDescription(description);
        attribute.setSortOrder(sortOrder != null ? sortOrder : 0);
        attribute.setIsFilterable(isFilterable != null ? isFilterable : false);
        attribute.setIsComparable(isComparable != null ? isComparable : true);
        attribute.setIsActive(true);

        return attributeRepo.save(attribute);
    }

    @Transactional
    public AttributeEntity updateAttribute(
            Long id,
            String name,
            AttributeEntity.DataType dataType,
            String unit,
            String description,
            Integer sortOrder,
            Boolean isFilterable,
            Boolean isComparable,
            Boolean isActive
    ) {
        log.info("Updating attribute: id={}, name={}, dataType={}", id, name, dataType);
        
        AttributeEntity attribute = attributeRepo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Attribute not found"));

        attribute.setName(name);
        if (dataType != null) {
            attribute.setDataType(dataType);
        }
        if (unit != null) {
            attribute.setUnit(unit);
        }
        if (description != null) {
            attribute.setDescription(description);
        }
        if (sortOrder != null) {
            attribute.setSortOrder(sortOrder);
        }
        if (isFilterable != null) {
            attribute.setIsFilterable(isFilterable);
        }
        if (isComparable != null) {
            attribute.setIsComparable(isComparable);
        }
        if (isActive != null) {
            attribute.setIsActive(isActive);
        }

        return attributeRepo.save(attribute);
    }

    @Transactional
    public void deleteAttribute(Long id) {
        log.info("Deleting attribute: id={}", id);
        
        AttributeEntity attribute = attributeRepo.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, "Attribute not found"));

        attributeRepo.delete(attribute);
    }
}
