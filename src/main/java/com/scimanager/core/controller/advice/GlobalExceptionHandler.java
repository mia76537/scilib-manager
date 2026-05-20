package com.scimanager.core.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.scimanager.core.common.Result;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 *
 * <p>统一捕获后端抛出的各类异常，避免将原始堆栈信息暴露给前端。<br>
 * 将业务逻辑中的错误映射为标准的 RESTful HTTP 状态码，统一返回 {@link Result} 包装对象。</p>
 *
 * <p><b>异常处理策略：</b></p>
 * <ul>
 *   <li>{@link RuntimeException} → HTTP 400（客户端错误，返回异常消息）</li>
 *   <li>{@link Exception} → HTTP 500（服务端错误，隐藏详细堆栈）</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 捕获业务逻辑异常（RuntimeException）
	 *
	 * <p>对应 Service 层通过 {@code throw new RuntimeException("错误信息")} 抛出的可控异常。<br>
	 * 异常消息会直接返回给前端展示。</p>
	 *
	 * @param e       捕获到的 RuntimeException 对象
	 * @param request 当前 HTTP 请求（可用于记录请求路径等上下文）
	 * @return HTTP 400 + Result.error(400, 异常消息)
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Result<String>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
		String errorMsg = e.getMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, errorMsg));
	}

	/**
	 * 捕获系统级未知异常（Exception）
	 *
	 * <p>处理如数据库连接超时、空指针、代码逻辑 Bug 等未预料的错误。<br>
	 * <b>安全策略：</b>不向客户端返回详细堆栈，仅提示"系统内部故障"。</p>
	 *
	 * @param e 捕获到的 Exception 对象
	 * @return HTTP 500 + Result.error(500, "系统内部故障，请联系管理员")
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Result<String>> handleException(Exception e) {
		e.printStackTrace(); // 在服务端控制台打印详细堆栈，便于开发调试
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error(500, "系统内部故障，请联系管理员"));
	}
}