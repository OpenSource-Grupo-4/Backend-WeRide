package org.example.backendweride.platform.iam.application.internal.outboundservices.security;

import org.example.backendweride.platform.iam.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the Account.id of the currently authenticated user from the SecurityContext.
 * UserDetailsImpl only carries the username, so this requires one repository lookup.
 */
@Component
public class AuthenticatedAccountProvider {

    private final AccountRepository accountRepository;

    public AuthenticatedAccountProvider(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Long getCurrentAccountId() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findByUserName(username)
                .map(org.example.backendweride.platform.iam.domain.model.aggregates.Account::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated account not found"));
    }
}
