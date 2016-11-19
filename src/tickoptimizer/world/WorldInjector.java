package tickoptimizer.world;

import java.lang.invoke.MethodHandle;

import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.PlayerChunkMap;
import net.minecraft.server.v1_11_R1.TileEntity;
import net.minecraft.server.v1_11_R1.World;
import net.minecraft.server.v1_11_R1.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;

import tickoptimizer.utils.Utils;
import tickoptimizer.utils.collections.HashSetBackedArrayList;
import tickoptimizer.utils.collections.HashSetFakeListImpl;

public class WorldInjector {

	private final static MethodHandle tileEntityListTickFieldSetter = Utils.getFieldSetter(World.class, "tileEntityListTick", HashSetBackedArrayList.class);
	private final static MethodHandle managedPlayersPlayersFieldSetter = Utils.getFieldSetter(PlayerChunkMap.class, "managedPlayers", HashSetFakeListImpl.class);
	private final static MethodHandle navigationListener = Utils.getFieldSetter(World.class, "t", OptimizedNavigationListener.class);

	public static void inject(org.bukkit.World world) {
		try {
			WorldServer nmsWorldServer = ((CraftWorld) world).getHandle();
			World nmsWorld = nmsWorldServer;
			tileEntityListTickFieldSetter.invoke(nmsWorld, new HashSetBackedArrayList<TileEntity>());
			navigationListener.invokeExact(nmsWorld, new OptimizedNavigationListener());
			PlayerChunkMap chunkmap = nmsWorldServer.getPlayerChunkMap();
			managedPlayersPlayersFieldSetter.invokeExact(chunkmap, new HashSetFakeListImpl<EntityPlayer>());
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
