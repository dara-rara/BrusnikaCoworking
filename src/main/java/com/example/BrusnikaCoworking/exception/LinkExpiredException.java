package com.example.BrusnikaCoworking.exception;

public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException(String msg){
        super(msg);
    }
}
