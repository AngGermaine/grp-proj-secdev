package com.secdev.project.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AssetRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.00", message = "Value must be non-negative")
    @DecimalMax(value = "100000000.00", message = "Value must be at most 100000000.00")
    @Digits(integer = 8, fraction = 2, message = "Max 2 decimal places")
    private BigDecimal value;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 100000, message = "Quantity must be at most 100000")
    private Integer quantity;

    public AssetRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}