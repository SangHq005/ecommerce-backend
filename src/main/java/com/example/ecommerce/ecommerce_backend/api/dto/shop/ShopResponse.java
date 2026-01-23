package com.example.ecommerce.ecommerce_backend.api.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private Long id;
    private Long sellerUserId;
    private String shopName;
    private String shopSlug;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String status;
    private String city;
    private String address;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String identityCode;
    private String taxCode;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
}
