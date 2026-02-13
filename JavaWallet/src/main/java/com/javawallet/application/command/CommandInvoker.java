package com.javawallet.application.command;

import com.javawallet.application.manager.FinanceManager;
import com.javawallet.domain.model.*;

import java.time.LocalDateTime;
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

    public void addTransaction(Wallet wallet, Money amount, TransactionType type, Category category, LocalDateTime date, String note) {
        executeCommand(new AddTransactionCommand(wallet, amount, type, category, date, note));
    }

    public void transfer(Wallet from, Wallet to, Money m, Category c, LocalDateTime date, String note) {
        executeCommand(new MakeTransferCommand(from, to, m, c, date, note));
    }

    public void createCategory(FinanceManager manager, Category category) {
        executeCommand(new NewCategoryCommand(manager, category));
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