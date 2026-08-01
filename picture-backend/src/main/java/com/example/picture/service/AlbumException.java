package com.example.picture.service;

/**
 * 相册业务校验异常，由 Controller 层统一转成 {code≠0, message} 的响应。
 */
public class AlbumException extends RuntimeException {
    public AlbumException(String message) {
        super(message);
    }
}
