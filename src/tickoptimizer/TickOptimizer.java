package tickoptimizer;

import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import tickoptimizer.keepalive.MoreKeepAliveThread;
import tickoptimizer.listeners.WorldListener;

public class TickOptimizer extends JavaPlugin {

	@Override
	public void onLoad() {
		try {
			ServerInjector.injectUserCache();
			ServerInjector.injectRegistry();
			new MoreKeepAliveThread().start();
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new WorldListener(), this);
	}

	@Override
	public void onDisable() {
		MinecraftServer.getServer().getUserCache().c();
		Bukkit.shutdown();
	}

}
