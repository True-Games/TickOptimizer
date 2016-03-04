package tickoptimizer.network;

import java.util.concurrent.TimeUnit;

import net.minecraft.server.v1_9_R1.PacketPlayOutKeepAlive;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class MoreKeepAliveThread extends Thread {

	public MoreKeepAliveThread() {
		super("TickOptimizer keep alive thread");
		setDaemon(true);
	}

	@Override
	public void run() {
		while (true) {
			try {
				for (Player player : Bukkit.getOnlinePlayers()) {
					((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutKeepAlive(1));
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(9));
			} catch (InterruptedException e) {
			}
		}
	}

}
