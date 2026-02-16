package com.javawallet.application.manager;

import com.javawallet.application.command.*;
import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.exception.object.TransactionNotFoundExecption;
import com.javawallet.domain.factory.IWalletFactory;
import com.javawallet.domain.model.*;
import com.javawallet.domain.visitor.Report;
import com.javawallet.infrastructure.persistence.IPersistenceContext;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class FinanceManager {

    private final IWalletFactory walletFactory;
    private final Report report;
    private final CommandInvoker commandInvoker;
    private final IPersistenceContext persistenceContext;


    public FinanceManager(IWalletFactory walletFactory,
                          Report report,
                          CommandInvoker commandInvoker,
                          IPersistenceContext persistenceContext) {
        this.walletFactory = walletFactory;
        this.report = report;
        this.commandInvoker = commandInvoker;
        this.persistenceContext = persistenceContext;
    }


    private IWalletRepository getWalletRepository(){
        return this.persistenceContext.getWalletRepository();
    }


    private ICategoryRepository getCategoryRepository(){
        return this.persistenceContext.getCategoryRepository();
    }

    public Wallet createWallet(String name,
                                WalletType walletType,
                                Money initialBalance){
        SaveWalletCommand command = new SaveWalletCommand(
                walletFactory.create(name, walletType, initialBalance),
                getWalletRepository());
        commandInvoker.execute(command);
        return command.getWallet();
    }

    public void removeWallet(UUID id) {
        RemoveWalletCommand command = new RemoveWalletCommand(id, getWalletRepository());
        commandInvoker.execute(command);
    }

    public Wallet getWallet(UUID id){
        return getWalletRepository().getWalletByUUID(id).get();
    }

    public void createCategory(String name){
        CreateCategoryCommand command = new CreateCategoryCommand(name, getCategoryRepository());
        commandInvoker.execute(command);
    }

    public void removeCategory(UUID id){
        RemoveCategoryCommand command = new RemoveCategoryCommand(id, getCategories(), getCategoryRepository());
        commandInvoker.execute(command);
    }

    public Collection<Category> getCategories(){
        return getCategoryRepository().loadCategories();
    }

    //create transaction con polimorfismo

    public void removeTransaction(UUID walletId, UUID transactionId){
        RemoveTransactionCommand command = new RemoveTransactionCommand(
                walletId,
                getTransaction(walletId, transactionId),
                getWalletRepository());
        commandInvoker.execute(command);
    }

    public Transaction getTransaction(UUID walletId, UUID transactionId){
        Wallet wallet = getWalletRepository().getWalletByUUID(walletId).get();
        Collection<Transaction> transactions = wallet.getTransactions();
        Optional<Transaction> transaction = transactions.stream().filter(t -> t.getId().equals(transactionId)).findAny();
        if (transaction.isEmpty()) throw new TransactionNotFoundExecption("Not found transaction with id " + transactionId.toString());
        return transaction.get();
    }

    public void undo(){
        commandInvoker.undo();
    }

    public void redo(){
        commandInvoker.redo();
    }
}