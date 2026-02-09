package com.javawallet.application.command;

import com.javawallet.application.manager.FinanceManager;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;

import java.util.Stack;

public class CommandInvoker {
    private final Stack<ICommand> commandHistory = new Stack<>();
    private final Stack<ICommand> redoStack = new Stack<>();

    private void executeCommand(ICommand command) {
        command.execute();
        commandHistory.push(command);
        redoStack.clear();
    }

    public void createWallet(FinanceManager manager, Wallet wallet) {
        executeCommand(new NewWalletCommand(manager, wallet));
    }

    public void addTransaction(Wallet wallet, Transaction transaction) {
        executeCommand(new AddTransactionCommand(wallet, transaction));
    }

    public void transfer(Wallet from, Wallet to, Transaction t) {
        executeCommand(new MakeTransferCommand(from, to, t));
    }

    public void undo() {
        if (commandHistory.isEmpty()) return;

        ICommand command = commandHistory.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;

        ICommand command = redoStack.pop();
        command.execute();
        commandHistory.push(command);
    }
}