package com.javawallet.domain.model;

import com.javawallet.domain.exception.domain.InvalidAmountException;
import java.time.LocalDateTime;

public class TransactionBuilder {
    private final Money amount;
    private final TransactionType type;
    private Category category;
    private String note;
    private LocalDateTime date;

    public TransactionBuilder(Money amount, TransactionType type) {
        if (amount == null) throw new InvalidAmountException("Amount is required to create transaction, cannot be null");
        if (type == null) throw new IllegalArgumentException("Type is required to create transaction, cannot be null");

        this.amount = amount;
        this.type = type;
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
        return new Transaction(amount, type, category, date, note);
    }
}