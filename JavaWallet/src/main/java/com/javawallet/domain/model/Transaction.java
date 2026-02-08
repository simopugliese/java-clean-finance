package com.javawallet.domain.model;

import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction implements IVisitable {
    private final UUID id;
    private final Money amount;
    private final TransactionType type;
    private final Category category;
    private LocalDateTime date;
    private String note;

    Transaction(UUID id, Money amount, TransactionType type, Category category, LocalDateTime date, String note) {
        this.id = id;
        this.amount = amount;
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
    public Money getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public Category getCategory() { return category; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date){this.date = date;}
    public String getNote() { return note; }
    public void setNote(String note) {this.note = note;}
}
