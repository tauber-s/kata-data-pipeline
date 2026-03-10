package com.example.spark.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Sale implements Serializable {
    private Integer id;
    private Timestamp date;
    private Integer productId;
    private Integer salesPersonId;

    public Sale() {}

    public Sale(Integer id, Timestamp date, Integer productId, Integer salesPersonId) {
        this.id = id;
        this.date = date;
        this.productId = productId;
        this.salesPersonId = salesPersonId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getSalesPersonId() { return salesPersonId; }
    public void setSalesPersonId(Integer salesPersonId) { this.salesPersonId = salesPersonId; }
}