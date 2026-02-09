package com.javawallet.application.command;

public interface ICommand {
    void execute();
    void undo();
}
