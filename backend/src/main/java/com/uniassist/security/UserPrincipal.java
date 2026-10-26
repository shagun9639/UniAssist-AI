package com.uniassist.security;

import com.uniassist.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String studentId;
    private final String password;
    private final String fullName;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String studentId, String password,
                         String fullName, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.studentId = studentId;
        this.password = password;
        this.fullName = fullName;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        String role = user.getRole();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getStudentId(),
                user.getPasswordHash(),
                user.getFullName(),
                authorities
        );
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
