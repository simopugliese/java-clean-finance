package com.javawallet.domain.visitor;

import com.javawallet.domain.model.Wallet;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Category;

public interface IVisitor {
    void visit(Wallet wallet);
    void visit(Transaction transaction);
    void visit(Category category);
}