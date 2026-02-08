package com.javawallet.domain.model;

import com.javawallet.domain.exception.object.TransactionNullException;
import com.javawallet.domain.strategy.IRuleStrategy;
import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

import java.util.Collection;
import java.util.UUID;

public class Wallet implements IVisitable {
    private final UUID id;
    private final String name;
    private final WalletType type;
    private Money balance;
    private final Collection<IRuleStrategy> ruleStrategies;
    private final Collection<Transaction> transactions;

    public Wallet(UUID id, String name, WalletType type, Money balance, Collection<IRuleStrategy> ruleStrategies, Collection<Transaction> transactions) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = balance;
        this.ruleStrategies = ruleStrategies;
        this.transactions = transactions;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public WalletType getType() { return type; }
    public Money getBalance() { return balance; }
    public Collection<IRuleStrategy> getRuleStrategy() { return ruleStrategies; }
    public Collection<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction t) {
        validateAndCheckRules(t);

        switch (t.getType()) {
            case DEPOSIT -> this.deposit(t.getAmount());
            case WITHDRAW -> this.withdraw(t.getAmount());
        }

        this.transactions.add(t);
    }

    public void removeTransaction(Transaction t) {
        validateAndCheckRules(t);

        switch (t.getType()) {
            case DEPOSIT -> this.withdraw(t.getAmount());
            case WITHDRAW -> this.deposit(t.getAmount());
        }

        this.transactions.remove(t);
    }

    private void validateAndCheckRules(Transaction t) {
        if (t == null) throw new TransactionNullException("Transaction was NULL");
        ruleStrategies.forEach(r -> r.check(this,t));
    }

    private void deposit(Money amount) {
        this.balance = this.balance.add(amount);
    }

    private void withdraw(Money amount) { this.balance = this.balance.subtract(amount); }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visit(this);
        for (Transaction t : transactions) {
            t.accept(visitor);
        }
    }
}
