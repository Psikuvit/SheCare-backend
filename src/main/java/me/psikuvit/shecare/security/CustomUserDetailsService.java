package me.psikuvit.shecare.security;

import lombok.RequiredArgsConstructor;
import me.psikuvit.shecare.model.User;
import me.psikuvit.shecare.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return new UserDetailsImpl(user);
    }
    
    public static class UserDetailsImpl implements UserDetails {
        
        private final String id;
        private final String email;
        private final String passwordHash;
        private final boolean enabled;
        private final Collection<? extends GrantedAuthority> authorities;
        
        public UserDetailsImpl(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.passwordHash = user.getPasswordHash();
            this.enabled = user.getEnabled();
            this.authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .collect(Collectors.toList());
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public String getPassword() {
            return passwordHash;
        }
        
        @Override
        public String getUsername() {
            return email;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return enabled;
        }
        
        public String getId() {
            return id;
        }
    }
}

