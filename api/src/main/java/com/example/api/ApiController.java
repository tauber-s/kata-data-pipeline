package com.example.api;

import com.example.api.dto.TopSalesPerCityDTO;
import com.example.api.dto.TopSalesmanCountryDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {
    private final ApiService apiService;

    @GetMapping("/top-sales-per-city")
    public List<TopSalesPerCityDTO> getTopSalesPerCity() {
        return apiService.getTopSalesPerCity();
    }

    @GetMapping("/top-salesman-country")
    public List<TopSalesmanCountryDTO> getTopSalesmanCountry() {
        return apiService.getTopSalesmanCountry();
    }
}
