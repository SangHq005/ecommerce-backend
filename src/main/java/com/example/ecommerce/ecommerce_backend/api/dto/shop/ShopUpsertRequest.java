package com.example.ecommerce.ecommerce_backend.api.dto.shop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopUpsertRequest {
    @NotBlank
    @Size(max = 191)
    @JsonProperty("shopName")
    private String shopName;

    @Size(max = 2000)
    @JsonProperty("description")
    private String description;

    @Size(max = 100)
    @JsonProperty("city")
    private String city;

    @Size(max = 255)
    @JsonProperty("address")
    private String address;

    @Size(max = 100)
    @JsonProperty("contactName")
    private String contactName;

    @Size(max = 20)
    @JsonProperty("contactPhone")
    private String contactPhone;

    @Size(max = 100)
    @JsonProperty("contactEmail")
    private String contactEmail;

    @Size(max = 50)
    @JsonProperty("identityCode")
    private String identityCode;

    @Size(max = 50)
    @JsonProperty("taxCode")
    private String taxCode;

    @Size(max = 100)
    @JsonProperty("bankName")
    private String bankName;

    @Size(max = 50)
    @JsonProperty("bankAccountNumber")
    private String bankAccountNumber;

    @Size(max = 100)
    @JsonProperty("bankAccountName")
    private String bankAccountName;

    // Direct methods for backward compatibility with controller code
    public String getShopName() { return shopName; }
    public String getDescription() { return description; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getContactName() { return contactName; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public String getIdentityCode() { return identityCode; }
    public String getTaxCode() { return taxCode; }
    public String getBankName() { return bankName; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public String getBankAccountName() { return bankAccountName; }
    
    // Also include record-style shorthand just in case
    public String shopName() { return shopName; }
    public String description() { return description; }
    public String city() { return city; }
    public String address() { return address; }
    public String contactName() { return contactName; }
    public String contactPhone() { return contactPhone; }
    public String contactEmail() { return contactEmail; }
    public String identityCode() { return identityCode; }
    public String taxCode() { return taxCode; }
    public String bankName() { return bankName; }
    public String bankAccountNumber() { return bankAccountNumber; }
    public String bankAccountName() { return bankAccountName; }
}
