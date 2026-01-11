package de.rollenspielwerkzeuge.dev.placemat.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentials(Locale locale) {
        String code = "auth.invalid_credentials";
        String msg = messageSource.getMessage(code, null, locale);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(code, msg));
    }
}