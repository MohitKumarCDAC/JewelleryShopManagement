package com.jewellery.jewelleryshop.dto;

import com.jewellery.jewelleryshop.entity.MetalType;

public class ManualBillItemDto {

    private Long id;
    private String itemName;
    private MetalType metalType;
    private Boolean active;

    // =========================
    // Constructors
    // =========================

    public ManualBillItemDto() {
    }

    public ManualBillItemDto(Long id, String itemName, MetalType metalType, Boolean active) {
        this.id = id;
        this.itemName = itemName;
        this.metalType = metalType;
        this.active = active;
    }

    // =========================
    // Getters & Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public MetalType getMetalType() {
        return metalType;
    }

    public void setMetalType(MetalType metalType) {
        this.metalType = metalType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}