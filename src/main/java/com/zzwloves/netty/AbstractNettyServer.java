 package com.zzwloves.netty;

import com.zzwloves.netty.websocket.server.WebSocketServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
 * netty 服务类
 * @author zhengwei.zhu
 * @version <b>1.0.0</b>
 */
public abstract class AbstractNettyServer implements NettyServer {
	private static Logger logger = LoggerFactory.getLogger(AbstractNettyServer.class);

	 NioEventLoopGroup bossGroup;
	 NioEventLoopGroup workerGroup;
	private ServerBootstrap serverBootstrap;
	private ServerConfig serverConfig;
	private ChannelFuture channelFuture;
	private ServerType serverType;

	public AbstractNettyServer(ServerConfig serverConfig, ServerType serverType) {
		this.serverConfig = serverConfig;
		this.serverType = serverType;
	}

	protected void createServer() {
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		try {
			serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup);
			serverBootstrap.channel(NioServerSocketChannel.class);
			serverBootstrap.childHandler(childHandler());

			/**
			 * 你可以设置这里指定的通道实现的配置参数。 我们正在写一个TCP/IP的服务端，
			 * 因此我们被允许设置socket的参数选项比如tcpNoDelay和keepAlive。
			 * 请参考ChannelOption和详细的ChannelConfig实现的接口文档以此可以对ChannelOptions的有一个大概的认识。
			 */
			serverBootstrap = serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
			/**
			 * option()是提供给NioServerSocketChannel用来接收进来的连接。
			 * childOption()是提供给由父管道ServerChannel接收到的连接，
			 * 在这个例子中也是NioServerSocketChannel。
			 */
			serverBootstrap = serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

			// 绑定端口
			channelFuture = serverBootstrap.bind(this.serverConfig.getPort());
		} catch (Exception e) {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	/**
	 * 服务创建并启动
	 * @author zhengwei.zhu
	 */
	@Override
	public void start() {
		try {
			// 开启服务
			channelFuture.sync();

			logger.info(serverType.name() + " started on port: {} with context path {}", serverConfig.getPort(), serverConfig.getContextPath());
			// 这里会一直等待，直到socket被关闭
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
	}

	protected abstract ChannelInitializer<SocketChannel> childHandler();

	 public ServerConfig getServerConfig() {
		 return serverConfig;
	 }

	 public void setServerConfig(ServerConfig serverConfig) {
		 this.serverConfig = serverConfig;
	 }

	 public ServerType getServerType() {
		 return serverType;
	 }

	 public void setServerType(ServerType serverType) {
		 this.serverType = serverType;
	 }
 }
