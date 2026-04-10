package com.scimanager.core.common;

import lombok.Data;

@Data
public class Result<T> {
	private int code;// 业务状态码
	private String message; // 提示信息
	private T data;// 泛型业务数据

	// 通用成功返回逻辑
	public static <T> Result<T> success(T data) {
		Result<T> result = new Result<>();
		result.setCode(200);
		result.setMessage("success");
		result.setData(data);
		return result;
	}

	// 通用错误返回逻辑
	public static <T> Result<T> error(int code, String message) {
		Result<T> result = new Result<>();
		result.setCode(code);
		result.setMessage(message);
		return result;
	}
}