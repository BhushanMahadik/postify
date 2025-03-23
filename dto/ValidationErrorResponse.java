package com.postify.main.dto;

import com.postify.main.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    
    private  String field;
    private String defaultMessage;
}
