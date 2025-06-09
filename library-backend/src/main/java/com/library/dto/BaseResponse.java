package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private String status;
    private String message;
    private long timestamp;
    private T data;
    
    public BaseResponse(String status, String message, long timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public BaseResponse(String status, long timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }
    
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>("SUCCESS", null, System.currentTimeMillis(), data);
    }
    
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>("SUCCESS", message, System.currentTimeMillis(), data);
    }
    
    public static <T> BaseResponse<T> success(String message) {
        return new BaseResponse<>("SUCCESS", message, System.currentTimeMillis(), null);
    }
    
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>("SUCCESS", null, System.currentTimeMillis(), null);
    }
    
    public static <T> BaseResponse<T> error(String status) {
        return new BaseResponse<>(status, null, System.currentTimeMillis(), null);
    }
    
    public static <T> BaseResponse<T> error(String status, String message) {
        return new BaseResponse<>(status, message, System.currentTimeMillis(), null);
    }
}