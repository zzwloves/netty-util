package com.zzwloves.netty.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzwloves.netty.AbstractNettyClient;
import com.zzwloves.netty.ClientType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Http客户端
 * @author: zhuzhengwei
 * @date: 2019/3/29 13:06
 */
public class HttpClient extends AbstractNettyClient {

	private final ConcurrentHashMap<String, Channel> map = new ConcurrentHashMap<>();

	public HttpClient() {
		super(null, ClientType.HTTP_CLIENT);
	}

	/**
	 * GET请求
	 * @param url 请求地址
	 * @param headers 自定义的请求头信息
	 * @param resultClass 返回结果类类型
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public <T> HttpResult<T> getRequest(String url, HttpHeaders headers, Class<T> resultClass) throws Exception {
		if (headers == null) {
			headers = new DefaultHttpHeaders();
		}
		return execute(HttpMethod.GET, url, headers, new EmptyByteBuf(ByteBufAllocator.DEFAULT), resultClass);
	}

	private <T> HttpResult<T> execute(HttpMethod httpMethod, String url, HttpHeaders headers, ByteBuf body, Class<T> resultClass) throws Exception {
		URI uri = new URI(url);
		Channel channel = map.computeIfAbsent(uri.getHost() + uri.getPort(), (k) -> {
			try {
				return getBootstrap().connect(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort()).sync().channel();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		});

		DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri.toASCIIString(), body);
		HttpHeaders httpHeaders = request.headers();
		httpHeaders.add(HttpHeaderNames.HOST, uri.getHost());
		httpHeaders.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
		httpHeaders.add(headers);

		channel.writeAndFlush(request);

		// 阻塞，等待结果
		DefaultChannelPromise resultFuture = new DefaultChannelPromise(channel);
		channel.attr(AttributeKey.valueOf("resultFuture")).set(resultFuture);
		resultFuture.sync();

		Integer resultCode = (Integer) channel.attr(AttributeKey.valueOf("resultCode")).getAndSet(null);
		HttpHeaders resultHeaders = (HttpHeaders) channel.attr(AttributeKey.valueOf("resultHeaders")).getAndSet(null);
		String resultBody = (String) channel.attr(AttributeKey.valueOf("resultBody")).getAndSet(null);
		return new HttpResult<T>(resultHeaders, resultCode, new ObjectMapper().readValue(resultBody, resultClass));
	}

	@Override
	protected ChannelInitializer<SocketChannel> handler() {

		return new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline()
//						.addLast("loggingHandler", new LoggingHandler(LogLevel.ERROR))
						.addLast("httpClientCodec", new HttpClientCodec())
						.addLast("httpObjectAggregator", new HttpObjectAggregator(1024 * 1024 * 10))
						.addLast("httpResponseHandler", new HttpResponseHandler());
			}
		};
	}

	@Override
	public void start() throws Exception {

	}

	class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
			Channel channel = ctx.channel();
			ByteBuf content = msg.content();
			channel.attr(AttributeKey.valueOf("resultCode")).set(msg.status().code());
			channel.attr(AttributeKey.valueOf("resultHeaders")).set(msg.headers());
			channel.attr(AttributeKey.valueOf("resultBody")).set(content.toString(CharsetUtil.UTF_8));
			content.clear();

			DefaultChannelPromise resultFuture = (DefaultChannelPromise) channel.attr(AttributeKey.valueOf("resultFuture")).getAndSet(null);
			resultFuture.setSuccess();
		}
	}

}