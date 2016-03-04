package tickoptimizer.world;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.PlayerChunkMap;
import net.minecraft.server.v1_9_R1.TileEntity;
import net.minecraft.server.v1_9_R1.World;
import net.minecraft.server.v1_9_R1.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

import tickoptimizer.utils.Utils;

public class WorldInjector {

	private final static MethodHandle loadedTileEntityListFieldSetter = Utils.getFieldSetter(World.class, "tileEntityList", VoidList.class);
	private final static MethodHandle tileEntityListFieldSetter = Utils.getFieldSetter(World.class, "tileEntityListTick", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle pendingTileEntityListFieldSetter = Utils.getFieldSetter(World.class, "b", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle entityListFieldSetter = Utils.getFieldSetter(World.class, "entityList", ArrayList.class);
	private final static MethodHandle managedPlayersPlayersFieldSetter = Utils.getFieldSetter(PlayerChunkMap.class, "managedPlayers", HashSetFakeListImpl.class);

	public static void inject(org.bukkit.World world) {
		try {
			WorldServer nmsWorldServer = ((CraftWorld) world).getHandle();
			World nmsWorld = nmsWorldServer;
			entityListFieldSetter.invokeExact(nmsWorld, new ArrayList<Entity>());
			loadedTileEntityListFieldSetter.invokeExact(nmsWorld, new VoidList<TileEntity>());
			pendingTileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			tileEntityListFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			PlayerChunkMap chunkmap = nmsWorldServer.getPlayerChunkMap();
			managedPlayersPlayersFieldSetter.invokeExact(chunkmap, new HashSetFakeListImpl<EntityPlayer>());
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
