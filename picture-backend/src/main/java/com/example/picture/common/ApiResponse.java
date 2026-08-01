package com.example.picture.common;

import lombok.Data;

/**
 * 相册相关接口的统一响应包裹结构。
 */
@Data
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<T>();
        resp.setCode(0);
        resp.setMessage("ok");
        resp.setData(data);
        return resp;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> resp = new ApiResponse<T>();
        resp.setCode(1);
        resp.setMessage(message);
        resp.setData(null);
        return resp;
    }
}
