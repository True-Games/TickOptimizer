package tickoptimizer.world;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import tickoptimizer.utils.Utils;

public class WorldInjector {

	private final static MethodHandle unusedTileEntityListFieldSetter = Utils.getFieldSetter(World.class, "h", VoidList.class);
	private final static MethodHandle pendingTileEntityListFieldSetter = Utils.getFieldSetter(World.class, "b", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle tileEntityListFieldSetter = Utils.getFieldSetter(World.class, "tileEntityList", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle entityListFieldSetter = Utils.getFieldSetter(World.class, "entityList", ArrayList.class);

	public static void inject(org.bukkit.World world) {
		try {
			World nmsWorld = ((CraftWorld) world).getHandle();
			entityListFieldSetter.invokeExact(nmsWorld, new ArrayList<Entity>());
			unusedTileEntityListFieldSetter.invokeExact(nmsWorld, new VoidList<TileEntity>());
			pendingTileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			tileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
