package com.javawallet.application.command;

import com.javawallet.application.manager.FinanceManager;
import com.javawallet.domain.model.Category;

class NewCategoryCommand implements ICommand {
    private final FinanceManager financeManager;
    private final Category category;

    public NewCategoryCommand(FinanceManager financeManager, Category category) {
        this.financeManager = financeManager;
        this.category = category;
    }

    @Override
    public void execute() {
        financeManager.addCategory(category);
    }

    @Override
    public void undo() {
        financeManager.removeCategory(category);
    }
}