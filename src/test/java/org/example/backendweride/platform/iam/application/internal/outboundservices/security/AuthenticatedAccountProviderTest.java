package org.example.backendweride.platform.iam.application.internal.outboundservices.security;

import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.iam.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticatedAccountProviderTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AuthenticatedAccountProvider provider = new AuthenticatedAccountProvider(accountRepository);

    private void authenticateAs(String username, String role) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        @SuppressWarnings("unchecked")
        java.util.Collection<? extends GrantedAuthority> authorities = java.util.List.of(authority);
        org.mockito.Mockito.doReturn(authorities).when(authentication).getAuthorities();
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
        authenticateAs("auditor@weride.com", "ROLE_CLIENT");
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(42L);
        when(accountRepository.findByUserName("auditor@weride.com")).thenReturn(Optional.of(account));

        Long id = provider.getCurrentAccountId();

        assertEquals(42L, id);
    }

    @Test
    void returnsAccountWithAdminRole() {
        authenticateAs("admin@weride.com", "ROLE_ADMIN");
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(1L);
        when(accountRepository.findByUserName("admin@weride.com")).thenReturn(Optional.of(account));

        var authenticated = provider.getCurrentAccount();

        assertEquals(1L, authenticated.id());
        assertEquals("ROLE_ADMIN", authenticated.role());
    }

    @Test
    void isCurrentUserAdmin_reflectsAuthorityRole() {
        authenticateAs("admin@weride.com", "ROLE_ADMIN");
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(1L);
        when(accountRepository.findByUserName("admin@weride.com")).thenReturn(Optional.of(account));

        assertEquals(true, provider.isCurrentUserAdmin());
    }

    @Test
    void throwsWhenAuthenticatedUsernameHasNoAccount() {
        authenticateAs("ghost@weride.com", "ROLE_CLIENT");
        when(accountRepository.findByUserName("ghost@weride.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, provider::getCurrentAccountId);
    }
}
