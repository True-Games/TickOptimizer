package tickoptimizer.world;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.minecraft.server.v1_8_R1.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;

import tickoptimizer.utils.Utils;

public class WorldInjector {

	private final static MethodHandle aFieldSetter = getAFieldSetter().asType(MethodType.methodType(void.class, World.class, TileEntityCanUpdateSkipArrayList.class));
	private final static MethodHandle hFieldSetter = getHFieldSetter().asType(MethodType.methodType(void.class, World.class, TileEntityCanUpdateSkipArrayList.class));
	private final static MethodHandle tileEntityListFieldSetter = getTileEntityListFieldSetter().asType(MethodType.methodType(void.class, World.class, TileEntityCanUpdateSkipArrayList.class));

	private static MethodHandle getAFieldSetter() {
		try {
			return MethodHandles.lookup().unreflectSetter(Utils.setAccessible(net.minecraft.server.v1_8_R1.World.class.getDeclaredField("a")));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}

	private static MethodHandle getHFieldSetter() {
		try {
			return MethodHandles.lookup().unreflectSetter(Utils.setAccessible(net.minecraft.server.v1_8_R1.World.class.getDeclaredField("h")));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}

	private static MethodHandle getTileEntityListFieldSetter() {
		try {
			return MethodHandles.lookup().unreflectSetter(Utils.setAccessible(net.minecraft.server.v1_8_R1.World.class.getDeclaredField("tileEntityList")));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
		return null;
	}


	public static void inject(org.bukkit.World world) {
		try {
			World nmsWorld = ((CraftWorld) world).getHandle();
			aFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			hFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			tileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
