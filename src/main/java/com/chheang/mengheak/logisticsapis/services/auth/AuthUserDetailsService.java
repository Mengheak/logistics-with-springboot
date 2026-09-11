package com.chheang.mengheak.logisticsapis.services.auth;

import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import com.chheang.mengheak.logisticsapis.models.AuthUser;
import com.chheang.mengheak.logisticsapis.repositories.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public AuthUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Deliberately reports a generic message: telling a caller that an email is unknown
     * would let them enumerate accounts.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
        return new AuthUser(user);
    }
}
