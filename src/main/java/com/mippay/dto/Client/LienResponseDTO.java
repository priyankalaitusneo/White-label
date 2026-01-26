package com.mippay.dto.Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LienResponseDTO {
    private boolean success;
    private String message;
    private Object data;
    
    public LienResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}