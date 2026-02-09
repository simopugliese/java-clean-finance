package com.javawallet.domain.model;

import com.javawallet.domain.exception.object.TransactionNullException;
import com.javawallet.domain.exception.transactionType.InvalidTransactionType;
import com.javawallet.domain.strategy.IRuleStrategy;
import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet implements IVisitable {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    private String name;
    private WalletType type;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "balance_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "balance_currency"))
    })
    private Money balance;

    @Transient
    private Collection<IRuleStrategy> ruleStrategies;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "wallet_id")
    private Collection<Transaction> transactions;

    protected Wallet(){}

    public Wallet(String name, WalletType type, Money balance, Collection<IRuleStrategy> ruleStrategies, Collection<Transaction> transactions) {
        this.id = UUID.randomUUID();
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
    public Collection<Transaction> getTransactions() { return Collections.unmodifiableCollection(this.transactions); }

    public void addTransaction(Transaction t) {
        validateAndCheckRules(t);

        switch (t.getType()) {
            case DEPOSIT -> this.deposit(t.getMoney());
            case WITHDRAW -> this.withdraw(t.getMoney());
            case TRANSFER ->  throw new InvalidTransactionType("Transaction type cannot be TRANSFER");
        }

        t.setWallet(this);

        this.transactions.add(t);
    }

    public void removeTransaction(Transaction t) {
        validateAndCheckRules(t);

        switch (t.getType()) {
            case DEPOSIT -> this.withdraw(t.getMoney());
            case WITHDRAW -> this.deposit(t.getMoney());
            case TRANSFER ->  throw new InvalidTransactionType("Transaction type cannot be TRANSFER");
        }

        this.transactions.remove(t);
    }

    public void transferWithdraw(Transaction t){
        validateAndCheckRules(t);
        withdraw(t.getMoney());
        t.setWallet(this);
        this.transactions.add(t);
    }

    public void transferDeposit(Transaction t){
        validateAndCheckRules(t);
        deposit(t.getMoney());
        t.setWallet(this);
        this.transactions.add(t);
    }

    public void rollbackTransferWithdraw(Transaction t) {
        deposit(t.getMoney());
        this.transactions.remove(t);
    }

    public void rollbackTransferDeposit(Transaction t) {
        withdraw(t.getMoney());
        this.transactions.remove(t);
    }

    private void validateAndCheckRules(Transaction t) {
        if (t == null) throw new TransactionNullException("Transaction was NULL");
        ruleStrategies.forEach(r -> r.check(this,t));
    }

    private void deposit(Money amount) {
        this.balance = this.balance.add(amount);
    }

    public void rollbackDeposit(Transaction t) {
        withdraw(t.getMoney());
        this.transactions.remove(t);
    }

    private void withdraw(Money amount) { this.balance = this.balance.subtract(amount); }

    public void rollbackWithdraw(Transaction t) {
        deposit(t.getMoney());
        this.transactions.remove(t);
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visit(this);
        //TODO: cancella
//        for (Transaction t : transactions) {
//            t.accept(visitor);
//        }
    }
}
