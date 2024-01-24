package com.shoppingbot.SpringBot.model;


import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.HashMap;
import java.util.Map;

@Entity(name="shopping_lists")
public class ShoppingList {
    @Id
    private Long id;

    private Long owner;

    private Long user;

    @ElementCollection
    private Map<String, Integer> list = new HashMap<>();

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

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public Map<String, Integer> getList() {
        return list;
    }

    public void setList(Map<String, Integer> list) {
        this.list = list;
    }
}
