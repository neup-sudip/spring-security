package com.example.security.models;

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
    private boolean result;
    private Object data;
    private String message;

    @Override
    public String toString() {
        return "ApiResponse{" +
                "result=" + result +
//                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
