package com.javawallet.application.command;

import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.domain.model.Category;

public class CreateCategoryCommand implements ICommand{
    private final Category category;
    private final ICategoryRepository categoryRepository;

    public CreateCategoryCommand(String name, ICategoryRepository categoryRepository){
        this.category = new Category(name);
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void execute() {
        this.categoryRepository.save(this.category);
    }

    @Override
    public void undo() {
        this.categoryRepository.remove(this.category.getId());
    }
}
