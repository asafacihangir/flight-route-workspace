package org.phoenix.flightrouteapi.security.web.error;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    private static final String GENERIC_INVALID_CREDENTIALS = "Invalid username or password";

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ProblemDetail handleBadCredentials(AuthenticationException ex) {
        return unauthorized(GENERIC_INVALID_CREDENTIALS);
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLocked(LockedException ex) {
        return unauthorized("Account is locked");
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabled(DisabledException ex) {
        return unauthorized("Account is disabled");
    }

    @ExceptionHandler(AccountExpiredException.class)
    public ProblemDetail handleAccountExpired(AccountExpiredException ex) {
        return unauthorized("Account has expired");
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ProblemDetail handleCredentialsExpired(CredentialsExpiredException ex) {
        return unauthorized("Credentials have expired");
    }

    private ProblemDetail unauthorized(String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detail);
        problem.setTitle("Unauthorized");
        return problem;
    }
}
