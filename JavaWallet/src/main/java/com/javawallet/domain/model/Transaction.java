package com.javawallet.domain.model;

import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction implements IVisitable {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Embedded
    private Money money;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id")
    private Category category;

    private LocalDateTime date;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    protected Transaction(){}

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
