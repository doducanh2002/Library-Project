package com.library.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessReturnRequestDTO {
    
    @Size(max = 500, message = "Return notes cannot exceed 500 characters")
    private String returnNotes;
    
    private BigDecimal customFineAmount; // Optional: Override calculated fine
    
    private Boolean damageReported = false;
    
    @Size(max = 1000, message = "Damage description cannot exceed 1000 characters")
    private String damageDescription;
}