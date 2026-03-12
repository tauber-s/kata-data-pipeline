package com.example.api;

import com.example.api.dto.TopSalesPerCityDTO;
import com.example.api.dto.TopSalesPersonCountryDTO;
import com.example.api.entity.Product;
import com.example.api.entity.SalesPerson;
import com.example.api.entity.TopSalesPerCity;
import com.example.api.entity.TopSalesPersonCountry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ApiService {
    @PersistenceContext
    private EntityManager entityManager;

    public List<TopSalesPerCityDTO> getTopSalesPerCity() {
        List<TopSalesPerCity> topSales = entityManager.createQuery(
                "SELECT t FROM TopSalesPerCity t ORDER BY t.totalSales DESC", TopSalesPerCity.class
        ).getResultList();

        return topSales.stream()
                .map(t -> {
                    Product product = entityManager.find(Product.class, t.getProductId());
                    return new TopSalesPerCityDTO(
                            t.getCity(),
                            product != null ? product.getName() : null,
                            t.getTotalSales()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<TopSalesPersonCountryDTO> getTopSalesPersonCountry() {
        List<TopSalesPersonCountry> topSales = entityManager.createQuery(
                "SELECT t FROM TopSalesPersonCountry t ORDER BY t.totalSales DESC", TopSalesPersonCountry.class
        ).getResultList();

        return topSales.stream()
                .map(t -> {
                    SalesPerson sp = entityManager.find(SalesPerson.class, t.getSalesPersonId());
                    return new TopSalesPersonCountryDTO(
                            sp != null ? sp.getName() : null,
                            t.getTotalSales()
                    );
                })
                .collect(Collectors.toList());
    }
}
