package tickoptimizer.world;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PlayerChunkMap;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import tickoptimizer.utils.Utils;

public class WorldInjector {

	private final static MethodHandle loadedTileEntityListFieldSetter = Utils.getFieldSetter(World.class, "h", VoidList.class);
	private final static MethodHandle pendingTileEntityListFieldSetter = Utils.getFieldSetter(World.class, "b", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle tileEntityListFieldSetter = Utils.getFieldSetter(World.class, "tileEntityList", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle entityListFieldSetter = Utils.getFieldSetter(World.class, "entityList", ArrayList.class);
	private final static MethodHandle managedPlayersPlayersFieldSetter = Utils.getFieldSetter(PlayerChunkMap.class, "managedPlayers", HashSetFakeListImpl.class);
	private final static MethodHandle playerChunksToUpdateFieldSetter = Utils.getFieldSetter(PlayerChunkMap.class, "e", LinkedHashSetQueue.class);
	private final static MethodHandle playerChunksUpdatingFieldSetter = Utils.getFieldSetter(PlayerChunkMap.class, "f", LinkedHashSetQueue.class);

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
			playerChunksToUpdateFieldSetter.invokeExact(chunkmap, new LinkedHashSetQueue<>());
			playerChunksUpdatingFieldSetter.invokeExact(chunkmap, new LinkedHashSetQueue<>());
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
