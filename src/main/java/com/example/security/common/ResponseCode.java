package com.example.security.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    SUCCESS("000", "Success"),
    FAILED("001", "Failed"),
    EXCEPTION("E001", "Exception"),
    UNAUTHORIZED("401", "Not Authorized");

    private final String code;
    private final String message;
}