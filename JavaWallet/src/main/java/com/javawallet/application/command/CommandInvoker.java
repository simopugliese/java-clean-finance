package com.javawallet.application.command;

import com.javawallet.domain.model.*;

import java.util.Stack;

public class CommandInvoker {
    private final Stack<ICommand> undoStack = new Stack<>();
    private final Stack<ICommand> redoStack = new Stack<>();

    private void execute(ICommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;

        ICommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;

        ICommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }
}