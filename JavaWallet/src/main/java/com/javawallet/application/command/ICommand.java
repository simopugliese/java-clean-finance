package com.javawallet.application.command;

interface ICommand {
    void execute();
    void undo();
}
