package vn.edu.iuh.fit.bookstorebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.edu.iuh.fit.bookstorebackend.model.RestRespone;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<RestRespone<Object>> handleException(Exception exception) {
        RestRespone<Object> res = new RestRespone<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(exception.getMessage());

        
        String userMessage = "Validation Error";
        if (exception instanceof BadCredentialsException
                || exception instanceof CredentialsExpiredException
                || (exception.getMessage() != null && (
                        exception.getMessage().toLowerCase().contains("invalid credentials") ||
                        exception.getMessage().toLowerCase().contains("old password") ||
                        exception.getMessage().toLowerCase().contains("not authenticated") ||
                        exception.getMessage().toLowerCase().contains("user not found")
                ))) {
            userMessage = exception.getMessage();
        }

        res.setMessage(userMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
