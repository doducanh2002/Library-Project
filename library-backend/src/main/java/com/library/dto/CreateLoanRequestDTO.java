package com.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanRequestDTO {
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    @Size(max = 500, message = "User notes cannot exceed 500 characters")
    private String userNotes;
}