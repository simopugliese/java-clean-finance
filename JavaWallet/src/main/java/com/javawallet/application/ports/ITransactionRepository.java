package com.javawallet.application.ports;

import com.javawallet.domain.model.Transaction;
import java.util.List;
import java.util.UUID;

public interface ITransactionRepository {
    void save(Transaction transaction);
    List<Transaction> findByWalletId(UUID walletId);
}