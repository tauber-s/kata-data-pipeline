package com.example.api.dto;

public record TopSalesPerCityDTO(
        String city,
        String productName,
        Double totalSales
) {}
