package com.javawallet.application.ports;

import com.javawallet.domain.model.Category;

import java.util.Collection;
import java.util.UUID;

public interface ICategoryRepository {
    boolean save(Category category);
    Collection<Category> loadCategories();
    Collection<Category> loadSubcategories(UUID parentId);
    boolean delete(UUID id);
}