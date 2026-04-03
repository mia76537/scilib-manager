package com.scimanager.core.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.scimanager.core.common.Result;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器 (Global Exception Handler)
 * * 作用：
 * 1. 统一捕获后端抛出的各类异常，避免将原始堆栈信息暴露给前端。
 * 2. 将业务逻辑中的错误映射为标准的 RESTful HTTP 状态码。
 * 3. 统一返回格式为 Result 包装对象。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务逻辑异常 
     * * @param e 异常对象
     * @param request 请求对象，用于记录日志或获取路径
     * @return 带有特定 HTTP 状态码的 Result 对象
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<String>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String errorMsg = e.getMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, errorMsg));
    }

    /**
     * 捕获系统级未知异常 (Exception)
     * 处理如数据库连接超时、空指针、代码逻辑 Bug 等未预料的错误
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e) {
        // 在控制台打印详细堆栈
        e.printStackTrace(); 
        
        // 返回 500 Internal Server Error
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "系统内部故障，请联系管理员"));
    }
}