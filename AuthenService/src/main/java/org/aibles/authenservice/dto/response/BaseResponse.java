package org.aibles.authenservice.dto.response;

public class BaseResponse<T> {

    private String code;

    private Long timestamp;

    private T data;

    public BaseResponse() {
    }

    public BaseResponse(String code, Long timestamp, T data) {
        this.code = code;
        this.timestamp = timestamp;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
