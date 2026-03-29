package me.psikuvit.shecare.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    
    @Id
    private String id;
    
    private String userId;
    
    private String tokenHash;
    
    private Long expiryTime;
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

