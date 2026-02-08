package com.javawallet.domain.model;

import com.javawallet.domain.exception.domain.InsufficientFundsException;
import com.javawallet.domain.exception.object.TransactionNullException;
import com.javawallet.domain.strategy.IRuleStrategy;
import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

import java.util.Collection;
import java.util.UUID;

public class Wallet implements IVisitable {
    private final UUID id;
    private final String name;
    private Money balance;
    private final IRuleStrategy ruleStrategy;
    private final Collection<Transaction> transactions;

    Wallet(UUID id, String name, Money balance, IRuleStrategy ruleStrategy, Collection<Transaction> transactions) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.ruleStrategy = ruleStrategy;
        this.transactions = transactions;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Money getBalance() { return balance; }
    public IRuleStrategy getRuleStrategy() { return ruleStrategy; }
    public Collection<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction t){
        if (t == null){
            throw new TransactionNullException("Transaction was null");
        }
        if (t.getType().equals(TransactionType.DEPOSIT)){
            this.deposit(t.getAmount());
        }
        if (t.getType().equals(TransactionType.WITHDRAW)){
            this.withdraw(t.getAmount());
        }
        this.transactions.add(t);
    }

    public void removeTransaction(Transaction t){
        if (t == null){
            throw new TransactionNullException("Transaction was NULL");
        }
        if (t.getType().equals(TransactionType.DEPOSIT)){
            this.withdraw(t.getAmount());
        }
        if (t.getType().equals(TransactionType.WITHDRAW)){
            this.deposit(t.getAmount());
        }
        this.transactions.remove(t);
    }

    private void deposit(Money amount) {
        this.balance = this.balance.add(amount);
    }

    private void withdraw(Money amount) {
        if (!ruleStrategy.canWithdraw(balance, amount)) {
            throw new InsufficientFundsException("Wallet id: "  + id.toString());
        }
        this.balance = this.balance.subtract(amount);
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visit(this);
        for (Transaction t : transactions) {
            t.accept(visitor);
        }
    }
}
