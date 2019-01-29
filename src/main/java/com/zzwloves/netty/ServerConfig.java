package com.zzwloves.netty;

/**
 * 服务配置类
 * @author zhengwei.zhu
 * @version <b>1.0.0</b>
 */
public class ServerConfig {
	
	public static final int DEFAULT_PORT = 80;
	public static final String DEFAULT_CONTEXT_PATH = "/";

	private int port;
	private String contextPath;
	private NettyHandler nettyHandler;
	private HandshakeInterceptorChain interceptorChain;
	
	public ServerConfig(int port, String contextPath, NettyHandler nettyHandler, HandshakeInterceptorChain handshakeInterceptorChain) {
		this.port = port;
		this.contextPath = contextPath;
		this.nettyHandler = nettyHandler;
		this.interceptorChain = handshakeInterceptorChain;
	}

	//*************************** get/set **************************************************
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public NettyHandler getNettyHandler() {
		return nettyHandler;
	}

	public void setNettyHandler(NettyHandler nettyHandler) {
		this.nettyHandler = nettyHandler;
	}

	public HandshakeInterceptorChain getInterceptorChain() {
		return interceptorChain;
	}

	public void setInterceptorChain(HandshakeInterceptorChain interceptorChain) {
		this.interceptorChain = interceptorChain;
	}

	// ***************************** toString ************************************************
	@Override
	public String toString() {
		return "WebSocketServerConfig {port=" + port + ", contextPath="
				+ contextPath + "}";
	}
		
		
}
