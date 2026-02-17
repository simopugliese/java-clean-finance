package com.javawallet.domain.model;

import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction implements IVisitable {
    private final UUID id;
    private final Category category;
    private final TransactionType type;
    private final Money money;
    private LocalDateTime date;
    private String note;

    Transaction(Money money, TransactionType type, Category category, LocalDateTime date, String note) {
        this.id = UUID.randomUUID();
        this.category = category;
        this.type = type;
        this.money = money;
        this.date = date;
        this.note = note;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }

    public UUID getId() {return id;}
    public Category getCategory() { return category; }
    public TransactionType getType() { return type; }
    public Money getMoney() { return money; }
    public LocalDateTime getDate() { return date; }
    public String getNote() { return note; }
    public void setDate(LocalDateTime date){this.date = date;}
    public void setNote(String note) {this.note = note;}

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", category=" + category +
                ", type=" + type +
                ", money=" + money +
                ", date=" + date +
                ", note='" + note + '\'' +
                '}';
    }
}
