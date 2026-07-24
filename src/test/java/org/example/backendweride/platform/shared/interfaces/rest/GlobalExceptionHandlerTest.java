package org.example.backendweride.platform.shared.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsIllegalArgumentExceptionToNotFound() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("Account not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account not found", response.getBody());
    }

    @Test
    void mapsDuplicateRuntimeExceptionToConflict() {
        var response = handler.handleRuntimeException(new RuntimeException("User already exists"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void mapsNotFoundRuntimeExceptionToNotFound() {
        var response = handler.handleRuntimeException(new RuntimeException("User not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void mapsInvalidCredentialsRuntimeExceptionToUnauthorized() {
        var response = handler.handleRuntimeException(new RuntimeException("Invalid credentials"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void unrecognizedRuntimeExceptionFallsBackToInternalServerError() {
        var response = handler.handleRuntimeException(new RuntimeException("Something unexpected"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void mapsValidationExceptionToBadRequestWithFieldErrors() throws NoSuchMethodException {
        var target = new Object();
        var bindingResult = new MapBindingResult(new HashMap<>(), "resource");
        bindingResult.addError(new org.springframework.validation.FieldError("resource", "brand", "must not be blank"));
        MethodParameter param = new MethodParameter(this.getClass().getDeclaredMethod("mapsValidationExceptionToBadRequestWithFieldErrors"), -1);
        var ex = new MethodArgumentNotValidException(param, bindingResult);

        var response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(true, response.getBody().contains("brand"));
    }
}
