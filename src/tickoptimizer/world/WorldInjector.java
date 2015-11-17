package tickoptimizer.world;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import tickoptimizer.utils.Utils;

public class WorldInjector {

	private final static MethodHandle toRemoveTileEntityListFieldSetter = Utils.getFieldSetter(World.class, "h", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle pendingTileEntityListFieldSetter = Utils.getFieldSetter(World.class, "b", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle tileEntityListFieldSetter = Utils.getFieldSetter(World.class, "tileEntityList", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle entityListFieldSetter = Utils.getFieldSetter(World.class, "entityList", ArrayList.class);

	public static void inject(org.bukkit.World world) {
		try {
			World nmsWorld = ((CraftWorld) world).getHandle();
			entityListFieldSetter.invokeExact(nmsWorld, new ArrayList<Entity>());
			toRemoveTileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			pendingTileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			tileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
