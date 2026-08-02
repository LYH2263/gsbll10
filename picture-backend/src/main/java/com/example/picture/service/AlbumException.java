package com.example.picture.service;

public class AlbumException extends RuntimeException {
    private final int code;

    public AlbumException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
