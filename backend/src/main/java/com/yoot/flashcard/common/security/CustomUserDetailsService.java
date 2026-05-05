package com.yoot.flashcard.common.security;

import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findWithRolesAndPermissionsByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserPrincipal.from(user);
    }
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findWithRolesAndPermissionsById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserPrincipal.from(user);
    }
}
