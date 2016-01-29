package tickoptimizer.network;

import java.lang.invoke.MethodHandle;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.NetworkManager;
import tickoptimizer.utils.Utils;

public class NetworkManagerInjector {

	private static final MethodHandle queueLockSetter = Utils.getFieldSetter(NetworkManager.class, "j", FakeReadWriteLock.class);

	private static final FakeReadWriteLock fakeLock = new FakeReadWriteLock();

	public static void inject(Player player) {
		try {
			NetworkManager nm = ((CraftPlayer) player).getHandle().playerConnection.networkManager;
			queueLockSetter.invokeExact(nm, fakeLock);
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
