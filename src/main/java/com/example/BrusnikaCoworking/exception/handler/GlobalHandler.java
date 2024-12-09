package com.example.BrusnikaCoworking.exception.handler;

import com.example.BrusnikaCoworking.exception.Base64OperationException;
import com.example.BrusnikaCoworking.exception.EmailRegisteredException;
import com.example.BrusnikaCoworking.exception.LinkExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.function.BiFunction;


@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(Base64OperationException.class)
    public ResponseEntity<ProblemDetail> handle(Base64OperationException e) {
        return withDetails.apply(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(EmailRegisteredException.class)
    public ResponseEntity<ProblemDetail> handle(EmailRegisteredException e) {
        return withDetails.apply(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(LinkExpiredException.class)
    public ResponseEntity<ProblemDetail> handle(LinkExpiredException e) {
        return withDetails.apply(HttpStatus.NOT_FOUND, e);
    }

    private final BiFunction<HttpStatus, RuntimeException, ResponseEntity<ProblemDetail>>
            withDetails = (status, ex) -> {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        return new ResponseEntity<>(problemDetail, status);
    };
}
