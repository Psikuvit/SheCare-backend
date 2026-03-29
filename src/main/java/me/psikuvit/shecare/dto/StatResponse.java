package me.psikuvit.shecare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatResponse {
    
    private String label;
    
    private Object value; // Can be String or Number
    
    private String change;
    
    private String icon;
}

