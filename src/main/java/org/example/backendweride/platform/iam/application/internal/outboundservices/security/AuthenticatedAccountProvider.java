package org.example.backendweride.platform.iam.application.internal.outboundservices.security;

import org.example.backendweride.platform.iam.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated account (id + role) from the SecurityContext.
 * UserDetailsImpl only carries the username, so this requires one repository lookup.
 */
@Component
public class AuthenticatedAccountProvider {

    private final AccountRepository accountRepository;

    public AuthenticatedAccountProvider(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * @return the id and role of the currently authenticated account.
     */
    public AuthenticatedAccount getCurrentAccount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var username = authentication.getName();
        var role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CLIENT");
        var account = accountRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Authenticated account not found"));
        return new AuthenticatedAccount(account.getId(), role);
    }

    /**
     * Convenience accessor for the current account id.
     */
    public Long getCurrentAccountId() {
        return getCurrentAccount().id();
    }

    public boolean isCurrentUserAdmin() {
        return getCurrentAccount().role().equals("ROLE_ADMIN");
    }

    /**
     * Value object describing the authenticated account: its numeric id and its role.
     */
    public record AuthenticatedAccount(Long id, String role) {}
}
