package org.example.backendweride.platform.iam.application.internal.outboundservices.security;

import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.iam.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticatedAccountProviderTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AuthenticatedAccountProvider provider = new AuthenticatedAccountProvider(accountRepository);

    private void authenticateAs(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAccountIdForAuthenticatedUsername() {
        authenticateAs("auditor@weride.com");
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(42L);
        when(accountRepository.findByUserName("auditor@weride.com")).thenReturn(Optional.of(account));

        Long id = provider.getCurrentAccountId();

        assertEquals(42L, id);
    }

    @Test
    void throwsWhenAuthenticatedUsernameHasNoAccount() {
        authenticateAs("ghost@weride.com");
        when(accountRepository.findByUserName("ghost@weride.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, provider::getCurrentAccountId);
    }
}
