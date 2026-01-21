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
        
        String errorMessage = exception.getMessage();
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = exception.getClass().getSimpleName();
        }
        
        res.setError(errorMessage);
        
        String userMessage = "Validation Error";
        if (exception instanceof BadCredentialsException
                || exception instanceof CredentialsExpiredException
                || (errorMessage != null && (
                        errorMessage.toLowerCase().contains("invalid credentials") ||
                        errorMessage.toLowerCase().contains("old password") ||
                        errorMessage.toLowerCase().contains("not authenticated") ||
                        errorMessage.toLowerCase().contains("user not found")
                ))) {
            userMessage = errorMessage;
        } else if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            userMessage = errorMessage;
        }

        res.setMessage(userMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
