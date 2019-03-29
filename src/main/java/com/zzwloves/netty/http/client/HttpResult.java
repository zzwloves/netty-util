package com.zzwloves.netty.http.client;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * 请求返回结果
 * @author: zhuzhengwei
 * @date: 2019/3/29 14:11
 */
public class HttpResult<T> {
	private HttpHeaders headers;
	private int HttpStatusCode;
	private T Body;

	public HttpResult(HttpHeaders headers, int httpStatusCode, T body) {
		this.headers = headers;
		HttpStatusCode = httpStatusCode;
		Body = body;
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public int getHttpStatusCode() {
		return HttpStatusCode;
	}

	public T getBody() {
		return Body;
	}

}