package com.javawallet.application.command;

import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.domain.exception.object.CategoryNullException;
import com.javawallet.domain.model.Category;

import java.util.Collection;
import java.util.UUID;

public class RemoveCategoryCommand implements ICommand{
    private final Category category;
    private final ICategoryRepository categoryRepository;

    public RemoveCategoryCommand(
            UUID id,
            Collection<Category> categories,
            ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        this.category = categories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(()->new CategoryNullException("Category not found with id " + id.toString()));
    }

    @Override
    public void execute() {
        categoryRepository.delete(this.category.getId());
    }

    @Override
    public void undo() {
        categoryRepository.save(this.category);
    }
}
