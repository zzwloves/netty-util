package com.zzwloves.netty;

import java.util.Map;

import com.zzwloves.netty.NettyHandler;
import com.zzwloves.netty.websocket.handler.WebSocketHandler;

/**
 * 握手拦截器
 * @author zhengwei.zhu
 * @version <b>1.0.0</b>
 */
public interface HandshakeInterceptor {

	/**
	 * 握手前
	 * @author zhengwei.zhu
	 * @param serverRequest
	 * @param handler
	 * @param attributes
	 * @return
	 * @throws Exception
	 */
	boolean beforeHandshake(ServerRequest serverRequest, NettyHandler handler, Map<String, Object> attributes) throws Exception;

	/**
	 * 握手后
	 * @author zhengwei.zhu
	 * @param handler
	 * @param exception
	 */
	void afterHandshake(NettyHandler handler, Exception exception);
}
