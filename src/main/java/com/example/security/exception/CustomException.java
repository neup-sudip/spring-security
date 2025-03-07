package com.example.security.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomException extends RuntimeException{
    private int status;

    public CustomException(String error, int status) {
        super(error);
        this.status = status;
    }
}
