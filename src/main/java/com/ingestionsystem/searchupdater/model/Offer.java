package com.ingestionsystem.searchupdater.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Offer {
    @Id
    private String id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id")
    private Product product;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Offer offer = (Offer) o;
        return Objects.equals(id, offer.id) && Objects.equals(name, offer.name) && Objects.equals(product, offer.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, product);
    }
}
