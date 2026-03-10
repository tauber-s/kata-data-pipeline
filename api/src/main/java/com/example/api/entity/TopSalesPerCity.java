package com.example.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "top_sales_per_city")
@NoArgsConstructor
@Getter
@Setter
public class TopSalesPerCity {
    @Id
    private String city;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "total_sales", nullable = false)
    private Double totalSales;
}
