package vn.edu.iuh.fit.bookstorebackend.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.edu.iuh.fit.bookstorebackend.model.RestRespone;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // Xử lý validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestRespone<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null 
                                ? error.getDefaultMessage() 
                                : "Invalid value"
                ));

        RestRespone<Object> res = new RestRespone<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Validation failed");
        res.setMessage(errors);  
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // Xử lý authentication & các exception khác
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<RestRespone<Object>> handleException(Exception exception) {
        RestRespone<Object> res = new RestRespone<>();
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
