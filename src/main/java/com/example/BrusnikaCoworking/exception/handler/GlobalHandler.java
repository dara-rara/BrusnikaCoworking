package com.example.BrusnikaCoworking.exception.handler;

import com.example.BrusnikaCoworking.adapter.web.auth.dto.MessageResponse;
import com.example.BrusnikaCoworking.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(CodingException.class)
    public ResponseEntity<MessageResponse> handle(CodingException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<MessageResponse> handle(EmailException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<MessageResponse> handle(PasswordException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceException.class)
    public ResponseEntity<MessageResponse> handle(ResourceException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(LinkExpiredException.class)
    public ResponseEntity<MessageResponse> handle(LinkExpiredException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(ReservalException.class)
    public ResponseEntity<MessageResponse> handle(ReservalException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<MessageResponse> handle(InternalServerErrorException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse(ex.getMessage()));
    }

//    private final BiFunction<HttpStatus, RuntimeException, ResponseEntity<ProblemDetail>>
//            withDetails = (status, ex) -> {
//        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
//        return new ResponseEntity<>(problemDetail, status);
//    };
}
