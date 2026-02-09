package com.javawallet.domain.model;

import com.javawallet.domain.exception.domain.InvalidAmountException;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionBuilder {
    private final UUID id;
    private final Money amount;
    private final TransactionType type;
    private Category category;
    private String note;
    private LocalDateTime date;

    public TransactionBuilder(Money amount, TransactionType type, LocalDateTime date) {
        if (amount == null) throw new InvalidAmountException("Amount is required to create transaction, cannot be null");
        if (type == null) throw new IllegalArgumentException("Type is required to create transaction, cannot be null");

        this.id = UUID.randomUUID();
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public TransactionBuilder withCategory(Category category) {
        this.category = category;
        return this;
    }

    public TransactionBuilder withNote(String note) {
        this.note = note;
        return this;
    }

    public TransactionBuilder withDate(LocalDateTime date) {
        this.date = date;
        return this;
    }

    public Transaction build() {
        return new Transaction(id, amount, type, category, date, note);
    }
}