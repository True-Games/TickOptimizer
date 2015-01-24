package tickoptimizer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.List;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import tickoptimizer.netty.EpollServerConnection;
import tickoptimizer.utils.Utils;
import net.minecraft.server.v1_8_R1.MinecraftServer;
import net.minecraft.server.v1_8_R1.NetworkManager;
import net.minecraft.server.v1_8_R1.ServerConnection;

public class NettyInjector {

	@SuppressWarnings("unchecked")
	public static void injectEpoll() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InterruptedException {
		ServerConnection serverConnection = MinecraftServer.getServer().getServerConnection();
		List<ChannelFuture> connections = ((List<ChannelFuture>) Utils.setAccessible(serverConnection.getClass().getDeclaredField("f")).get(serverConnection));
		Channel channel = connections.get(0).channel();
		ChannelHandler serverHandler = channel.pipeline().first();
		ChannelInitializer<Channel> initializer = (ChannelInitializer<Channel>) Utils.setAccessible(serverHandler.getClass().getDeclaredField("childHandler")).get(serverHandler);
		InetSocketAddress address = (InetSocketAddress) channel.localAddress();
		channel.config().setAutoRead(false);
		channel.disconnect().sync();
		channel.close().sync();
		connections.clear();
		connections.add(
			new ServerBootstrap()
			.channel(EpollServerSocketChannel.class)
			.childHandler(initializer)
			.group(new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Server IO #%d").setDaemon(true).build()))
			.localAddress(address.getAddress(), address.getPort())
			.bind()
			.syncUninterruptibly()
		);
		Utils.setAccessible(MinecraftServer.class.getDeclaredField("q")).set(
			MinecraftServer.getServer(),
			new EpollServerConnection(
				(List<NetworkManager>) Utils.setAccessible(serverConnection.getClass().getDeclaredField("g")).get(serverConnection),
				connections
			)
		);
	}

}
