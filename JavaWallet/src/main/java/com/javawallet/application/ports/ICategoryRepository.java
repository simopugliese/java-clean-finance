package com.javawallet.application.ports;

import com.javawallet.domain.model.Category;

import java.util.Collection;
import java.util.UUID;

public interface ICategoryRepository {
    void save(Category category);
    Collection<Category> loadCategories();
    Collection<Category> loadSubcategories(UUID parentId);
    void remove(UUID id);
}