package com.shoppingbot.SpringBot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;


import java.util.LinkedList;

@Entity(name="shopping_lists")
public class ShoppingList {
    @Id
    private Long id;

    private Long owner;

    private Long associatedUser;

    @OneToMany(mappedBy = "shoppingList")
    private LinkedList<Product> products = new LinkedList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public Long getAssociatedUser() {
        return associatedUser;
    }

    public void setAssociatedUser(Long associatedUser) {
        this.associatedUser = associatedUser;
    }

    public LinkedList<Product> getProducts() {
        return products;
    }

    public void setProducts(LinkedList<Product> products) {
        this.products = products;
    }
}
