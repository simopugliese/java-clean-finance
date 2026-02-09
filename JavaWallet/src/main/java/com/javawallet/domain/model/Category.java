package com.javawallet.domain.model;

import com.javawallet.domain.exception.object.CategoryNullException;
import com.javawallet.domain.visitor.IVisitable;
import com.javawallet.domain.visitor.IVisitor;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category implements IVisitable {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Category> children;

    protected Category(){}

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
        //TODO: cancella
//        for (Category child : children) {
//            child.accept(visitor);
//        }
    }
}
