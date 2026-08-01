package com.example.picture.common;

/**
 * 业务异常：message 为可读中文错误信息，由全局异常处理器包装为统一响应。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
