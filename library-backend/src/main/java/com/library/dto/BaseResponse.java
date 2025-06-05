package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private String status;
    private long timestamp;
    private T data;
    
    public BaseResponse(String status, long timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }
    
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), data);
    }
    
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), null);
    }
    
    public static <T> BaseResponse<T> error(String status) {
        return new BaseResponse<>(status, System.currentTimeMillis(), null);
    }
}