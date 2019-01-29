package com.zzwloves.netty.websocket.client;

import java.net.URI;

import com.zzwloves.netty.AbstractNettyClient;
import com.zzwloves.netty.ClientConfig;
import com.zzwloves.netty.ClientType;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;

import com.zzwloves.netty.websocket.WebSocketSession;
import com.zzwloves.netty.websocket.handler.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket 客户端
 * @author zhengwei.zhu
 * @version <b>1.0.0</b>
 */
public class WebSocketClient extends AbstractNettyClient<WebSocketSession> {

	private static Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
	
	public WebSocketClient(String url, HttpHeaders headers, WebSocketHandler webSocketHandler) {
		this(new ClientConfig(url, headers, webSocketHandler,
				new WebSocketClientHandShakeHandler(webSocketHandler),
				new WebSocketClientMessageHandler(webSocketHandler)),
				ClientType.WEBSOCKET_CLIENT);
	}
	
	private WebSocketClient(ClientConfig clientConfig, ClientType clientType) {
		super(clientConfig, clientType);
	}

	@Override
	protected ChannelInitializer<SocketChannel> handler() {
		return new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				ChannelPipeline pipeline = socketChannel.pipeline();
				pipeline.addLast("loggingHandler", new LoggingHandler(LogLevel.ERROR));
				pipeline.addLast("httpClientCodec", new HttpClientCodec());
				pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(1024 * 1024 * 10));
				for (ChannelHandler channelHandler : getClientConfig().getChannelHandlers()) {
					String simpleName = channelHandler.getClass().getSimpleName();
					String name = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
					pipeline.addLast(name, channelHandler);
				}
			}
		};
	}

	@Override
	public void start() throws Exception {
		connect();
		ChannelFuture channelFuture = getChannelFuture();
		Channel channel = channelFuture.channel();
		ClientConfig clientConfig = getClientConfig();
		HttpHeaders httpHeaders = clientConfig.getHeaders() != null ? clientConfig.getHeaders() : new DefaultHttpHeaders();
		// 进行握手
		WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
				clientConfig.getUri(), WebSocketVersion.V13, (String)null, true, httpHeaders);
		WebSocketClientHandShakeHandler handShakeHandler = channel.pipeline().get(WebSocketClientHandShakeHandler.class);
		handShakeHandler.setHandshaker(handshaker);
		handshaker.handshake(channel);
		//阻塞等待是否握手成功
		handShakeHandler.handshakeFuture().sync().addListener(future -> logger.info("握手成功"));

		handShakeHandler.webSocketSesssionFuture().sync();

		// 这里会一直等待，直到socket被关闭
		channel.closeFuture().sync().addListener(future -> {
			logger.error("连接已断开");
		});
	}

	public URI getURI() {
		return getClientConfig().getUri();
	}

	public HttpHeaders getHeaders() {
		return getClientConfig().getHeaders();
	}


	public WebSocketHandler getWebSocketHandler() {
		return (WebSocketHandler) getClientConfig().getNettyHandler();
	}

	public WebSocketSession getWebSocketSession() {
		ChannelFuture channelFuture = getChannelFuture();
		if (channelFuture == null) {
			throw new RuntimeException("未先调用start方法后");
		}
		return (WebSocketSession) getChannelFuture().channel().attr(AttributeKey.valueOf("webSocketSession")).get();
	}

}
