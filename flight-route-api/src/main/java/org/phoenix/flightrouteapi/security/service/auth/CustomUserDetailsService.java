package org.phoenix.flightrouteapi.security.service.auth;

import org.phoenix.flightrouteapi.security.domain.AuthoritiesConstants;
import org.phoenix.flightrouteapi.security.domain.User;
import org.phoenix.flightrouteapi.security.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        LOG.debug("Authenticating {}", login);

        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);

        User user = userRepository.findByUsername(lowercaseLogin)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User " + lowercaseLogin + " was not found in the database"));

        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                List.of(new SimpleGrantedAuthority(AuthoritiesConstants.ROLE_PREFIX + user.getRole().name()))
        );
    }
}
