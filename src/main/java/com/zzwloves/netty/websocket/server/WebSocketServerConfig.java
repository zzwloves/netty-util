package com.zzwloves.netty.websocket.server;

import com.zzwloves.netty.ServerConfig;
import com.zzwloves.netty.websocket.handler.WebSocketHandler;
import com.zzwloves.netty.HandshakeInterceptor;
import com.zzwloves.netty.HandshakeInterceptorChain;

/**
 * 服务配置类
 * @author zhengwei.zhu
 * @version <b>1.0.0</b>
 */
public class WebSocketServerConfig extends ServerConfig {
	
	private WebSocketHandler webSocketHandler;

	public WebSocketServerConfig(WebSocketHandler webSocketHandler, HandshakeInterceptor[] interceptors) {
		this(webSocketHandler, new HandshakeInterceptorChain(interceptors));
	}
	
	public WebSocketServerConfig(WebSocketHandler webSocketHandler, HandshakeInterceptorChain handshakeInterceptorChain) {
		this(DEFAULT_PORT, DEFAULT_CONTEXT_PATH, webSocketHandler, handshakeInterceptorChain);
	}

	public WebSocketServerConfig(int port, String contextPath, WebSocketHandler webSocketHandler, HandshakeInterceptor[] interceptors) {
		this(port, contextPath, webSocketHandler, new HandshakeInterceptorChain(interceptors));
	}
	
	public WebSocketServerConfig(int port, String contextPath, WebSocketHandler webSocketHandler, HandshakeInterceptorChain handshakeInterceptorChain) {
		super(port, contextPath, webSocketHandler, handshakeInterceptorChain);
		this.webSocketHandler = webSocketHandler;

	}

	public WebSocketHandler getWebSocketHandler() {
		return webSocketHandler;
	}

	public void setWebSocketHandler(WebSocketHandler webSocketHandler) {
		this.webSocketHandler = webSocketHandler;
	}

}
