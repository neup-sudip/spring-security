package com.example.security.models;

import com.example.security.common.ResponseCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse implements Serializable {
    private String code;
    private boolean result;
    private Object data;
    private String message;

    public static ApiResponse failed(String message){
        return new ApiResponse(ResponseCode.FAILED.getCode(), false, null, message);
    }

    public static ApiResponse failed(Object data, String message){
        return new ApiResponse(ResponseCode.FAILED.getCode(), false, data, message);
    }

    public static ApiResponse success(Object data, String message){
        return new ApiResponse(ResponseCode.SUCCESS.getCode(), true, data, message);
    }

    public static ApiResponse exception(String message){
        return new ApiResponse(ResponseCode.EXCEPTION.getCode(), false, null, message);
    }

    public static ApiResponse unauthorized(String message){
        return new ApiResponse(ResponseCode.UNAUTHORIZED.getCode(), false, null, message);
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "result=" + result +
//                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
