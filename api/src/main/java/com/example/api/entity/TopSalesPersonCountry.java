package com.example.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "top_salesman_country")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TopSalesPersonCountry {
    @Id
    @Column(name = "sales_person_id")
    private Integer salesPersonId;

    @Column(name = "total_sales", nullable = false)
    private Double totalSales;
}
