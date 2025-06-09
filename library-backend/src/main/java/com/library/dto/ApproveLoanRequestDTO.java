package com.library.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveLoanRequestDTO {
    
    @Size(max = 500, message = "Librarian notes cannot exceed 500 characters")
    private String notesByLibrarian;
    
    private Integer customLoanPeriodDays; // Optional: Override default loan period
}