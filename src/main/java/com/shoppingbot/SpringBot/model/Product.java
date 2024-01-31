package com.shoppingbot.SpringBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "products")
public class Product {
    @Id
    private long id;
    private String name;
    private Category category;

    private String typeOfProduct;

    private double quantity;

    private UnitOfMeasure unitOfMeasure;

    @ManyToOne
    @JoinColumn(name = "shopping_list_id")
    private ShoppingList shoppingList;

}
