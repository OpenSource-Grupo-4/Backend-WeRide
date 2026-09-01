package org.example.backendweride.platform.iam.infrastructure.auth.pipeline;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.example.backendweride.platform.iam.infrastructure.auth.model.UserDetailsImpl;
import org.example.backendweride.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BearerAuthRequestFilterTest {

    private final BearerTokenService tokenService = mock(BearerTokenService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final Logger logger = (Logger) LoggerFactory.getLogger(BearerAuthRequestFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        logger.detachAppender(appender);
        appender.stop();
        logger.setLevel(originalLevel);
    }

    @Test
    void missingTokenIsDebugAndDoesNotAttemptValidation() throws Exception {
        var request = new MockHttpServletRequest();
        when(tokenService.getBearerTokenFrom(request)).thenReturn(null);

        filter().doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertLog(Level.DEBUG, "No JWT token provided");
        verify(tokenService, never()).validateToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void invalidTokenIsWarn() throws Exception {
        var request = new MockHttpServletRequest();
        when(tokenService.getBearerTokenFrom(request)).thenReturn("invalid-token");
        when(tokenService.validateToken("invalid-token")).thenReturn(false);

        filter().doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertLog(Level.WARN, "Invalid JWT token");
    }

    @Test
    void validTokenAuthenticatesNormally() throws Exception {
        var request = new MockHttpServletRequest();
        var userDetails = new UserDetailsImpl("valid-user", "encoded-password", List.of());
        when(tokenService.getBearerTokenFrom(request)).thenReturn("valid-token");
        when(tokenService.validateToken("valid-token")).thenReturn(true);
        when(tokenService.getUsernameFromToken("valid-token")).thenReturn("valid-user");
        when(userDetailsService.loadUserByUsername("valid-user")).thenReturn(userDetails);

        filter().doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("valid-user", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(userDetailsService).loadUserByUsername("valid-user");
    }

    private BearerAuthRequestFilter filter() {
        return new BearerAuthRequestFilter(tokenService, userDetailsService);
    }

    private void assertLog(Level level, String message) {
        var matchingEvents = appender.list.stream()
                .filter(event -> event.getLevel().equals(level))
                .filter(event -> event.getFormattedMessage().equals(message))
                .toList();
        assertEquals(1, matchingEvents.size());
    }
}
