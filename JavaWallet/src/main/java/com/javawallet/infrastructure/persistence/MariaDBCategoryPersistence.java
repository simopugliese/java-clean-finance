package com.javawallet.infrastructure.persistence;

import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.domain.model.Category;

import java.util.Optional;
import java.util.UUID;

public class MariaDBCategoryPersistence implements ICategoryRepository {
    @Override
    public void save(Category category) {

    }

    @Override
    public void delete(Category category) {

    }

    @Override
    public Optional<Category> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByName(String name) {
        return Optional.empty();
    }
}