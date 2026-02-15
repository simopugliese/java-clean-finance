package com.javawallet.domain.visitor;

import com.javawallet.domain.model.Category;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;

public class Report implements IVisitor{
    public void generate(){

    }

    @Override
    public void visit(Wallet wallet) {

    }

    @Override
    public void visit(Transaction transaction) {

    }

    @Override
    public void visit(Category category) {

    }
}
