package com.javawallet.domain.model;

import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction implements IVisitable {
    private final UUID id;
    private final Money money;
    private final TransactionType type;
    private final Category category;
    private LocalDateTime date;
    private String note;

    private Wallet wallet;

    Transaction(UUID id, Money money, TransactionType type, Category category, LocalDateTime date, String note) {
        this.id = id;
        this.money = money;
        this.type = type;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }

    public UUID getId() {return id;}
    public Money getMoney() { return money; }
    public TransactionType getType() { return type; }
    public Category getCategory() { return category; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date){this.date = date;}
    public String getNote() { return note; }
    public void setNote(String note) {this.note = note;}
    protected void setWallet(Wallet wallet) { this.wallet = wallet; }
}
