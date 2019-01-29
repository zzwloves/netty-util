package com.zzwloves.netty.websocket.server;

import com.zzwloves.netty.AbstractNettyServer;
import com.zzwloves.netty.ServerConfig;
import com.zzwloves.netty.ServerType;
import com.zzwloves.netty.websocket.handler.WebSocketHandler;
import com.zzwloves.netty.HandshakeInterceptor;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * websocket服务实体类
 * @author zhengwei.zhu
 * @version <b>1.0.0</b>
 */
public final class WebScoketServer extends AbstractNettyServer {

	private int port;
	private String contextPath;

	//************************** 构造 ***************************************************
	public WebScoketServer(WebSocketHandler webSocketHandler, HandshakeInterceptor... interceptors) {
		this(ServerConfig.DEFAULT_PORT, ServerConfig.DEFAULT_CONTEXT_PATH, webSocketHandler, interceptors);
	}

	public WebScoketServer(int port, String contextPath, WebSocketHandler webSocketHandler, HandshakeInterceptor... interceptors) {
		super(new WebSocketServerConfig(port, contextPath, webSocketHandler, interceptors), ServerType.WEBSOCKET_SERVER);
		this.port = port;
		this.contextPath = contextPath;
		createServer();
	}

	//**************************** method *************************************************
	@Override
	public void start() {
		// 新建线程异步启动服务，原因：开启服务最后会进行线程阻塞
		Thread thread = new Thread(super::start);
		thread.setName("server-thread");
		thread.start();
	}

	@Override
	protected ChannelInitializer<SocketChannel> childHandler() {
		return new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch)
					throws Exception {
				ChannelPipeline channelPipeline = ch.pipeline();
				// 设置30秒没有读到数据，则触发一个READER_IDLE事件。
				// pipeline.addLast(new IdleStateHandler(30, 0, 0));
				// HttpServerCodec：将请求和应答消息解码为HTTP消息
				channelPipeline.addLast("httpServercodec",new HttpServerCodec());
				// HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
				channelPipeline.addLast("aggregator",new HttpObjectAggregator(1024*1024*10));
				// ChunkedWriteHandler：向客户端发送HTML5文件
				channelPipeline.addLast("httpChunked",new ChunkedWriteHandler());
				// 在管道中添加我们自己的接收数据实现方法
				channelPipeline.addLast("handShakeHandler",new WebSocketServerHandShakeHandler((WebSocketServerConfig) getServerConfig()));
				channelPipeline.addLast("messageHandler",new WebSocketServerMessageHandler((WebSocketServerConfig) getServerConfig()));

			}
		};
	}

	//**************************** get/set *************************************************
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

}
