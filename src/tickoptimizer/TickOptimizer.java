package tickoptimizer;

import net.minecraft.server.v1_12_R1.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TickOptimizer extends JavaPlugin {

	@Override
	public void onLoad() {
		try {
			ServerInjector.injectUserCache();
			ServerInjector.injectRegistry();
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new InjectorListener(), this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDisable() {
		MinecraftServer.getServer().getUserCache().c();
		Bukkit.shutdown();
	}

}
