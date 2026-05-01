package com.yoot.flashcard.common.security;

import com.yoot.flashcard.modules.identity.entity.Permission;
import com.yoot.flashcard.modules.identity.entity.Role;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonLocked;
    private final boolean enabled;

    private UserPrincipal(
            Long id,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            boolean accountNonLocked,
            boolean enabled
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
    }

    public static UserPrincipal from(User user) {
        Set<String> authorityNames = new TreeSet<>();
        for (Role role : user.getRoles()) {
            authorityNames.add("ROLE_" + role.getName());
            for (Permission permission : role.getPermissions()) {
                authorityNames.add(permission.getCode());
            }
        }

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                authorityNames.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()),
                user.getStatus() != UserStatus.LOCKED,
                user.getDeletedAt() == null && user.getStatus() == UserStatus.ACTIVE
        );
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
