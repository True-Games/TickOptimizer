package tickoptimizer.network;

import java.lang.invoke.MethodHandle;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_9_R2.NetworkManager;
import tickoptimizer.utils.Utils;

public class NetworkManagerInjector {

	private static final MethodHandle queueSetter = Utils.getFieldSetter(NetworkManager.class, "i", AlwaysEmptyQueue.class);
	private static final MethodHandle queueLockSetter = Utils.getFieldSetter(NetworkManager.class, "j", FakeReadWriteLock.class);

	private static final FakeReadWriteLock fakeLock = new FakeReadWriteLock();
	private static final AlwaysEmptyQueue<Object> emptyQueue = new AlwaysEmptyQueue<>();

	public static void inject(Player player) {
		try {
			NetworkManager nm = ((CraftPlayer) player).getHandle().playerConnection.networkManager;
			queueSetter.invokeExact(nm, emptyQueue);
			queueLockSetter.invokeExact(nm, fakeLock);
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
