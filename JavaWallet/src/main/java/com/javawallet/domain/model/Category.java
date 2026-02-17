package com.javawallet.domain.model;

import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;

import java.util.*;

public class Category implements IVisitable {
    private final UUID id;
    private final String name;
    private Category parent;
    private final Collection<Category> children;

    public Category(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Category getParent() { return parent; }
    public Collection<Category> getChildren() { return Collections.unmodifiableCollection(this.children); }

    private void setParent(Category parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parent=" + parent +
                ", children=" + Arrays.toString(children.toArray()) +
                '}';
    }

    public UUID addSubcategory(String name ) {
        Category c = new Category(name);
        c.setParent(this);
        this.children.add(c);
        return c.id;
    }
    public boolean removeSubcategory(UUID id, String name) {
        return this.children.removeIf(c -> c.getId().equals(id) && c.getName().equals(name));
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
