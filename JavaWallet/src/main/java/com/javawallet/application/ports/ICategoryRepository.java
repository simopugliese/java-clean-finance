package com.javawallet.application.ports;

import com.javawallet.domain.model.Category;
import java.util.Optional;
import java.util.UUID;

public interface ICategoryRepository {
    void save(Category category);
    void delete(Category category);
    Optional<Category> findById(UUID id);
    Optional<Category> findByName(String name);
}