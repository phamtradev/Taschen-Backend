package vn.edu.iuh.fit.bookstorebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        res.setMessage("Validation Error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
