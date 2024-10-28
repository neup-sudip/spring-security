package com.example.security.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean result;
    private Object data;
    private String message;

    public static ApiResponse failed(String message){
        return new ApiResponse(false, null, message);
    }

    public static ApiResponse success(Object data, String message){
        return new ApiResponse(true, data, message);
    }
}
