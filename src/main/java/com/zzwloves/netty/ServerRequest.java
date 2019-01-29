package com.zzwloves.netty;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.URI;

/**
 * @author: zhuzhengwei
 * @date: 2019/1/29 15:18
 */
public interface ServerRequest {

	URI URI();

}
