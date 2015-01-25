package tickoptimizer.netty;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.spigotmc.SpigotConfig;

import tickoptimizer.utils.Utils;
import net.minecraft.server.v1_8_R1.ChatComponentText;
import net.minecraft.server.v1_8_R1.CrashReport;
import net.minecraft.server.v1_8_R1.CrashReportSystemDetails;
import net.minecraft.server.v1_8_R1.MinecraftServer;
import net.minecraft.server.v1_8_R1.NetworkManager;
import net.minecraft.server.v1_8_R1.PacketPlayOutKickDisconnect;
import net.minecraft.server.v1_8_R1.ReportedException;
import net.minecraft.server.v1_8_R1.ServerConnection;

public class EpollServerConnection extends ServerConnection {

	private List<NetworkManager> g;
	private List<ChannelFuture> f;

	public EpollServerConnection(List<NetworkManager> networkManagers, List<ChannelFuture> connections) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		super(MinecraftServer.getServer());
		this.g = networkManagers;
		this.f = connections;
		Utils.setAccessible(ServerConnection.class.getDeclaredField("g")).set(this, this.g);
		Utils.setAccessible(ServerConnection.class.getDeclaredField("f")).set(this, this.f);
	}

	@SuppressWarnings("serial")
	public void c() {
		synchronized (this.g) {
			if (SpigotConfig.playerShuffle > 0 && MinecraftServer.currentTick % SpigotConfig.playerShuffle == 0) {
				Collections.shuffle(this.g);
			}
			final Iterator<NetworkManager> iterator = this.g.iterator();
			while (iterator.hasNext()) {
				final NetworkManager networkmanager = iterator.next();
				if (!networkmanager.h()) {
					if (!networkmanager.g() || networkmanager.j == null) {
						if (networkmanager.preparing) {
							continue;
						}
						iterator.remove();
						if (networkmanager.j == null) {
							networkmanager.j = new InetSocketAddress(-1) {
								@Override
						        public String toString() {
									return "TickOptimizerFakeAddress";
								}
							};
						}
						networkmanager.l();
					} else {
						try {
							networkmanager.a();
						} catch (Exception exception) {
							if (networkmanager.c()) {
								final CrashReport crashreport = CrashReport.a((Throwable) exception, "Ticking memory connection");
								final CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Ticking connection");
								crashreportsystemdetails.a("Connection", networkmanager.toString());
								throw new ReportedException(crashreport);
							}
							LogManager.getLogger().warn("Failed to handle packet for " + networkmanager.getSocketAddress(), (Throwable) exception);
							final ChatComponentText chatcomponenttext = new ChatComponentText("Internal server error");
							networkmanager.a(new PacketPlayOutKickDisconnect(chatcomponenttext), new GenericFutureListener<Future<?>>() {
								@Override
								public void operationComplete(final Future<?> future) {
									networkmanager.close(chatcomponenttext);
								}
							});
							networkmanager.k();
						}
					}
				}
			}
		}
	}

    @SuppressWarnings("rawtypes")
	static List a(final EpollServerConnection serverconnection) {
        return serverconnection.g;
    }

}
