
package com.vanexa.backend.service;

import com.vanexa.backend.entity.Category;
import com.vanexa.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(Category category) {

        if (categoryRepository.existsBySlug(category.getSlug().trim())) {
            throw new IllegalArgumentException(
                    "A category with this slug already exists");
        }

        if (categoryRepository.existsByNameIgnoreCase(
                category.getName().trim())) {
            throw new IllegalArgumentException(
                    "A category with this name already exists");
        }

        category.setName(category.getName().trim());
        category.setSlug(category.getSlug().trim());

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found"));
    }
}
