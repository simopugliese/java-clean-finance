package com.javawallet.domain.visitor;

public interface IVisitable {
    void accept(IVisitor visitor);
}