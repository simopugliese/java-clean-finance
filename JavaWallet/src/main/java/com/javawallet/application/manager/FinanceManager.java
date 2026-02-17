package com.javawallet.application.manager;

import com.javawallet.application.command.*;
import com.javawallet.application.ports.ICategoryRepository;
import com.javawallet.application.ports.IWalletRepository;
import com.javawallet.domain.exception.object.TransactionNotFoundExecption;
import com.javawallet.domain.factory.IWalletFactory;
import com.javawallet.domain.model.*;
import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;
import com.javawallet.infrastructure.persistence.IPersistenceContext;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class FinanceManager {

    private final IWalletFactory walletFactory;
    private final IVisitor visitor;
    private final CommandInvoker commandInvoker;
    private final IPersistenceContext persistenceContext;


    public FinanceManager(IWalletFactory walletFactory,
                          IVisitor visitor,
                          CommandInvoker commandInvoker,
                          IPersistenceContext persistenceContext) {
        this.walletFactory = walletFactory;
        this.visitor = visitor;
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

    public void createTransaction(
            UUID walletId,
            Money amount,
            TransactionType type
    ){
        Transaction transaction = new TransactionBuilder(amount, type).build();
        CreateTransactionCommand command = new CreateTransactionCommand(walletId, transaction, getWalletRepository());
        commandInvoker.execute(command);
    }

    public void createTransaction(
            UUID walletId,
            Money amount,
            TransactionType type,
            Category category
    ){
        Transaction transaction = new TransactionBuilder(amount, type)
                .withCategory(category)
                .build();
        CreateTransactionCommand command = new CreateTransactionCommand(walletId, transaction, getWalletRepository());
        commandInvoker.execute(command);
    }

    public void createTransaction(
            UUID walletId,
            Money amount,
            TransactionType type,
            Category category,
            String note
    ){
        Transaction transaction = new TransactionBuilder(amount, type)
                .withCategory(category)
                .withNote(note)
                .build();
        CreateTransactionCommand command = new CreateTransactionCommand(walletId, transaction, getWalletRepository());
        commandInvoker.execute(command);
    }

    public void createTransaction(
            UUID walletId,
            Money amount,
            TransactionType type,
            Category category,
            String note,
            LocalDateTime date
    ){
        Transaction transaction = new TransactionBuilder(amount, type)
                .withCategory(category)
                .withNote(note)
                .withDate(date)
                .build();
        CreateTransactionCommand command = new CreateTransactionCommand(walletId, transaction, getWalletRepository());
        commandInvoker.execute(command);
    }


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

    public void generateReport(IVisitable visitable){
        CreateReportCommand command = new CreateReportCommand(this.visitor, visitable);
        commandInvoker.execute(command);
    }

    public void undo(){
        commandInvoker.undo();
    }

    public void redo(){
        commandInvoker.redo();
    }
}