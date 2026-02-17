package com.javawallet.application.command;

import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

public class CreateReportCommand implements ICommand{
    private final IVisitable visitable;
    private final IVisitor visitor;

    public CreateReportCommand(IVisitor visitor, IVisitable visitable) {
        this.visitor = visitor;
        this.visitable = visitable;
    }

    @Override
    public void execute() {
        this.visitable.accept(visitor);
    }

    @Override
    public void undo() {

    }
}
