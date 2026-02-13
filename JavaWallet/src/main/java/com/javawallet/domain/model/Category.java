package com.javawallet.domain.model;

import com.javawallet.domain.exception.object.CategoryNullException;
import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class Category implements IVisitable {
    private final UUID id;
    private final String name;
    private Category parent;
    private final Collection<Category> children;

    public Category(String name, Collection<Category> children) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.children = (children != null) ? children : new ArrayList<>();
        for(Category c : this.children) {
            c.setParent(this);
        }
    }

    private void setParent(Category parent) {
        this.parent = parent;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Collection<Category> getChildren() { return children; }

    public void add(Category c){
        checkCollectionNull();
        c.setParent(this);
        children.add(c);
    }

    public void remove(Category c){
        checkCollectionNull();
        c.setParent(null);
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
    }
}
