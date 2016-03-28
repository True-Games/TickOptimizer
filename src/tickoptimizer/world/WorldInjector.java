package tickoptimizer.world;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import net.minecraft.server.v1_9_R1.ChunkProviderServer;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.MinecraftServer;
import net.minecraft.server.v1_9_R1.PlayerChunkMap;
import net.minecraft.server.v1_9_R1.TileEntity;
import net.minecraft.server.v1_9_R1.World;
import net.minecraft.server.v1_9_R1.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

import tickoptimizer.utils.Utils;

public class WorldInjector {

	private final static MethodHandle tileEntityListFieldSetter = Utils.getFieldSetter(World.class, "tileEntityList", HashSetFakeListImpl.class);
	private final static MethodHandle tileEntityListTickFieldSetter = Utils.getFieldSetter(World.class, "tileEntityListTick", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle tileEntityListPendingFieldSetter = Utils.getFieldSetter(World.class, "b", TileEntityCanUpdateSkipArrayList.class);
	private final static MethodHandle entityListFieldSetter = Utils.getFieldSetter(World.class, "entityList", ArrayList.class);
	private final static MethodHandle managedPlayersPlayersFieldSetter = Utils.getFieldSetter(PlayerChunkMap.class, "managedPlayers", HashSetFakeListImpl.class);
	private final static MethodHandle navigationListener = Utils.getFieldSetter(World.class, "t", OptimizedNavigationListener.class);
	private final static MethodHandle chunkLoaderFieldSetter = Utils.getFieldSetter(ChunkProviderServer.class, "chunkLoader", OptimizedChunkRegionLoader.class);

	@SuppressWarnings("deprecation")
	public static void inject(org.bukkit.World world) {
		try {
			WorldServer nmsWorldServer = ((CraftWorld) world).getHandle();
			World nmsWorld = nmsWorldServer;
			tileEntityListFieldSetter.invoke(nmsWorld, new HashSetFakeListImpl<TileEntity>());
			tileEntityListPendingFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			tileEntityListTickFieldSetter.invokeExact(nmsWorld, new TileEntityCanUpdateSkipArrayList());
			entityListFieldSetter.invokeExact(nmsWorld, new ArrayList<Entity>());
			navigationListener.invokeExact(nmsWorld, new OptimizedNavigationListener());
			PlayerChunkMap chunkmap = nmsWorldServer.getPlayerChunkMap();
			managedPlayersPlayersFieldSetter.invokeExact(chunkmap, new HashSetFakeListImpl<EntityPlayer>());
			ChunkProviderServer cps = nmsWorldServer.getChunkProviderServer();
			chunkLoaderFieldSetter.invokeExact(cps, new OptimizedChunkRegionLoader(nmsWorldServer.getWorld().getWorldFolder(), MinecraftServer.getServer().getDataConverterManager()));
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.shutdown();
		}
	}

}
