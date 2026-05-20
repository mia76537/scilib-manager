package com.scimanager.core.common;

import lombok.Data;

/**
 * 统一 REST 响应结果包装类
 *
 * <p>所有 Controller 接口统一使用此类封装返回数据，确保前端对接时格式一致。</p>
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

	/** 业务状态码（200 成功，其他为错误码） */
	private int code;

	/** 提示信息 */
	private String message;

	/** 泛型业务数据 */
	private T data;

	/**
	 * 通用成功返回
	 *
	 * @param data 业务数据
	 * @param <T>  数据类型
	 * @return Result 实例（code=200, message="success"）
	 */
	public static <T> Result<T> success(T data) {
		Result<T> result = new Result<>();
		result.setCode(200);
		result.setMessage("success");
		result.setData(data);
		return result;
	}

	/**
	 * 通用错误返回
	 *
	 * @param code    错误状态码
	 * @param message 错误描述
	 * @param <T>     数据类型
	 * @return Result 实例（data 为 null）
	 */
	public static <T> Result<T> error(int code, String message) {
		Result<T> result = new Result<>();
		result.setCode(code);
		result.setMessage(message);
		return result;
	}
}