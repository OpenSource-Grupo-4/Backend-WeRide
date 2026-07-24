package org.example.backendweride.platform.plan.application.internal.commands;

import org.example.backendweride.platform.plan.infrastructure.persistence.jpa.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class PlanCommandServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_returnsFalseWhenPlanDoesNotExist() {
        when(planRepository.existsById(99L)).thenReturn(false);

        boolean deleted = service.handle(99L);

        assertFalse(deleted);
        verify(planRepository, never()).deleteById(anyLong());
    }

    @Test
    void handle_returnsTrueAndDeletesWhenPlanExists() {
        when(planRepository.existsById(1L)).thenReturn(true);

        boolean deleted = service.handle(1L);

        assertTrue(deleted);
        verify(planRepository).deleteById(1L);
    }
}
