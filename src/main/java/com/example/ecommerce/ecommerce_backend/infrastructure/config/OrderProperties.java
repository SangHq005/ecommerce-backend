package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.order")
public class OrderProperties {
    private Long shippingFeeDefault = 30000L; // Default if not set in yaml
    private String currencyDefault = "VND";

    public Long getShippingFeeDefault() { return shippingFeeDefault; }
    public void setShippingFeeDefault(Long shippingFeeDefault) { this.shippingFeeDefault = shippingFeeDefault; }

    public String getCurrencyDefault() { return currencyDefault; }
    public void setCurrencyDefault(String currencyDefault) { this.currencyDefault = currencyDefault; }
}
