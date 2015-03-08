package tickoptimizer.world;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;

import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;

import tickoptimizer.utils.Utils;

public class WorldInjector {

	private final static MethodHandle unknownTileEntityListFieldSetter = getUnknownTileEntityListFieldSetter().asType(MethodType.methodType(void.class, World.class, TileEntityCanUpdateSkipArrayList.class));
	private final static MethodHandle pendingTileEntityListFieldSetter = getPendingTileEntityFieldSetter().asType(MethodType.methodType(void.class, World.class, TileEntityCanUpdateSkipArrayList.class));
	private final static MethodHandle tileEntityListFieldSetter = getTileEntityListFieldSetter().asType(MethodType.methodType(void.class, World.class, TileEntityCanUpdateSkipArrayList.class));
	private final static MethodHandle entityListFieldSetter = getEntityListFieldSetter().asType(MethodType.methodType(void.class, World.class, ArrayList.class));

	private static MethodHandle getUnknownTileEntityListFieldSetter() {
		try {
			return MethodHandles.lookup().unreflectSetter(Utils.setAccessible(net.minecraft.server.v1_8_R2.World.class.getDeclaredField("h")));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}

	private static MethodHandle getEntityListFieldSetter() {
		try {
			return MethodHandles.lookup().unreflectSetter(Utils.setAccessible(net.minecraft.server.v1_8_R2.World.class.getDeclaredField("entityList")));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}

	private static MethodHandle getPendingTileEntityFieldSetter() {
		try {
			return MethodHandles.lookup().unreflectSetter(Utils.setAccessible(net.minecraft.server.v1_8_R2.World.class.getDeclaredField("b")));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}

	private static MethodHandle getTileEntityListFieldSetter() {
		try {
			return MethodHandles.lookup().unreflectSetter(Utils.setAccessible(net.minecraft.server.v1_8_R2.World.class.getDeclaredField("tileEntityList")));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}


	public static void inject(org.bukkit.World world) {
		try {
			World nmsWorld = ((CraftWorld) world).getHandle();
			entityListFieldSetter.invokeExact(nmsWorld, new ArrayList<Entity>());
			unknownTileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			pendingTileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			tileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
