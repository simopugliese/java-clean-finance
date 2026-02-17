package com.javawallet.domain.visitor;

import com.javawallet.domain.model.*;

public class ReportCLI implements IVisitor{
    @Override
    public void visit(Wallet wallet) {
        System.out.println("Wallet id: " + wallet.getId().toString());
        System.out.println("Wallet name: " + wallet.getName());
        System.out.println("Wallet type: " + wallet.getType().toString());
        System.out.println("Wallet balance: " + wallet.getBalance().toString());
        System.out.println("Wallet Transactions:");
        wallet.getTransactions().forEach(t -> System.out.println(t.toString()));
        System.out.println("------------------------------------");
    }

    @Override
    public void visit(Transaction transaction) {
        System.out.println(transaction.toString());
    }

    @Override
    public void visit(Category category) {
        System.out.println(category.toString());
    }
}
