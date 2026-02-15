package com.javawallet.application.manager;

import com.javawallet.application.command.CommandInvoker;
import com.javawallet.domain.factory.WalletFactory;
import com.javawallet.domain.model.*;
import com.javawallet.domain.visitor.Report;
import com.javawallet.infrastructure.persistence.IPersistenceContext;

import java.util.UUID;

public class FinanceManager {

    private final WalletFactory walletFactory;
    private final Report report;
    private final CommandInvoker commandInvoker;
    private final IPersistenceContext persistenceContext;


    public FinanceManager(WalletFactory walletFactory,
                          Report report,
                          CommandInvoker commandInvoker,
                          IPersistenceContext persistenceContext) {
        this.walletFactory = walletFactory;
        this.report = report;
        this.commandInvoker = commandInvoker;
        this.persistenceContext = persistenceContext;
    }

    public boolean createWallet(String name, WalletType walletType, Money initialBalance){
        return false;
    }

    public boolean removeWallet(UUID id){
        return false;
    }

    public Wallet getWallet(UUID id){
        return null;
    }

    public boolean createCategory(){
        return false;
    }

}