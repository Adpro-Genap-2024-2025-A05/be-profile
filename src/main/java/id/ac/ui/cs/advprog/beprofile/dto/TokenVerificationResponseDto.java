package id.ac.ui.cs.advprog.beprofile.dto;

import id.ac.ui.cs.advprog.beprofile.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenVerificationResponseDto {
    private boolean valid;
    private String userId;
    private String email;
    private String userName; 
    private Role role;
    private Long expiresIn;
}