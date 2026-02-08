package com.javawallet.domain.model;

import com.javawallet.domain.exception.object.CategoryNullException;
import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

import java.util.Collection;

public class Category implements IVisitable {
    private final String name;
    private final Collection<Category> children;

    public String getName() {
        return name;
    }

    public Collection<Category> getChildren() {
        return children;
    }

    public Category(String name, Collection<Category> children) {
        this.name = name;
        this.children = children;
    }

    public void add(Category c){
        checkCollectionNull();
        children.add(c);
    }

    public void remove(Category c){
        checkCollectionNull();
        children.remove(c);
    }

    public void checkCollectionNull() {
        if (this.children == null) {
            throw new CategoryNullException("Category is null");
        }
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visit(this);
        for (Category child : children) {
            child.accept(visitor);
        }
    }
}
