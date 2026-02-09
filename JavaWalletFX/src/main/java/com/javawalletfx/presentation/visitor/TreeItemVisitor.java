package com.javawalletfx.presentation.visitor;

import com.javawallet.domain.model.Category;
import com.javawallet.domain.model.Transaction;
import com.javawallet.domain.model.Wallet;
import com.javawallet.domain.visitor.IVisitor;
import javafx.scene.control.TreeItem;

public class TreeItemVisitor implements IVisitor {

    // Questo stack (o semplice variabile di ritorno) tiene traccia dell'ultimo nodo creato
    private TreeItem<String> resultItem;

    public TreeItem<String> getResult() {
        return resultItem;
    }

    @Override
    public void visit(Wallet wallet) {
        // 1. Creo il nodo grafico per il Wallet
        TreeItem<String> walletItem = new TreeItem<>(wallet.getName() + " [" + wallet.getBalance() + "]");
        walletItem.setExpanded(true);

        // 2. ORA decido io di visitare i figli
        for (Transaction t : wallet.getTransactions()) {
            // Creo un nuovo visitor (o riuso questo resettando, ma new è più pulito per ricorsione)
            TreeItemVisitor childVisitor = new TreeItemVisitor();
            t.accept(childVisitor);

            // Aggiungo il risultato come figlio del Wallet
            walletItem.getChildren().add(childVisitor.getResult());
        }

        this.resultItem = walletItem;
    }

    @Override
    public void visit(Category category) {
        // 1. Creo il nodo per la Categoria
        TreeItem<String> categoryItem = new TreeItem<>(category.getName());
        categoryItem.setExpanded(true);

        // 2. Visito i sotto-figli
        for (Category child : category.getChildren()) {
            TreeItemVisitor childVisitor = new TreeItemVisitor();
            child.accept(childVisitor);
            categoryItem.getChildren().add(childVisitor.getResult());
        }

        this.resultItem = categoryItem;
    }

    @Override
    public void visit(Transaction transaction) {
        // Le transazioni sono foglie, creo solo il nodo
        String label = transaction.getDate().toLocalDate() + " - " + transaction.getMoney();
        this.resultItem = new TreeItem<>(label);
    }
}