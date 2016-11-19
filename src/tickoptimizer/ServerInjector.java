package tickoptimizer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.server.v1_11_R1.Block;
import net.minecraft.server.v1_11_R1.Blocks;
import net.minecraft.server.v1_11_R1.IBlockData;
import net.minecraft.server.v1_11_R1.Item;
import net.minecraft.server.v1_11_R1.ItemBlock;
import net.minecraft.server.v1_11_R1.MinecraftKey;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.RegistryMaterials;
import net.minecraft.server.v1_11_R1.TileEntity;
import net.minecraft.server.v1_11_R1.UserCache;

import org.bukkit.Bukkit;

import tickoptimizer.usercache.OptimizedUserCache;
import tickoptimizer.utils.Utils;
import tickoptimizer.world.block.InjectTEBlockBeacon;
import tickoptimizer.world.tileentity.OptimizedTileEntityBeacon;

public class ServerInjector {

	@SuppressWarnings("deprecation")
	public static void injectUserCache() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		UserCache oldUserCache = MinecraftServer.getServer().getUserCache();
		File oldUserCacheFile = (File) Utils.setAccessible(oldUserCache.getClass().getDeclaredField("h")).get(oldUserCache);
		OptimizedUserCache newUserCache = new OptimizedUserCache(MinecraftServer.getServer().getGameProfileRepository(), oldUserCacheFile);
		Utils.setFinalField(MinecraftServer.class.getDeclaredField("Y"), MinecraftServer.getServer(), newUserCache);
	}

	public static void injectRegistry() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		registerTileEntity("Beacon", OptimizedTileEntityBeacon.class);
		registerBlock(138, "beacon", new InjectTEBlockBeacon());

		fixBlocksRefs();

		Bukkit.resetRecipes();
	}

	@SuppressWarnings("unchecked")
	private static void registerTileEntity(String name, Class<? extends TileEntity> entityClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		RegistryMaterials<MinecraftKey, Class<? extends TileEntity>> registry = (RegistryMaterials<MinecraftKey, Class<? extends TileEntity>>) Utils.getFieldValue(null, TileEntity.class, "f");
		registry.a(new MinecraftKey(name), entityClass);
	}

	@SuppressWarnings("unchecked")
	private static void registerBlock(int id, String name, Block block) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		MinecraftKey stringkey = new MinecraftKey(name);
		ItemBlock itemblock = new ItemBlock(block);
		Block.REGISTRY.a(id, stringkey, block);
		Iterator<IBlockData> blockdataiterator = block.s().a().iterator();
		while (blockdataiterator.hasNext()) {
			IBlockData blockdata = blockdataiterator.next();
			final int stateId = (id << 4) | block.toLegacyData(blockdata);
			Block.REGISTRY_ID.a(blockdata, stateId);
		}
		Item.REGISTRY.a(id, stringkey, itemblock);
		((Map<Block, Item>)Utils.setAccessible(Item.class.getDeclaredField("a")).get(null)).put(block, itemblock);
	}

	private static void fixBlocksRefs() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		for (Field field : Blocks.class.getDeclaredFields()) {
			field.setAccessible(true);
			if (Block.class.isAssignableFrom(field.getType())) {
				Block block = (Block) field.get(null);
				Block newblock = Block.getById(Block.getId(block));
				if (block != newblock) {
					Iterator<IBlockData> blockdataiterator = block.s().a().iterator();
					while (blockdataiterator.hasNext()) {
						IBlockData blockdata = blockdataiterator.next();
						Utils.setFieldValue(blockdata, "a", block);
					}
					Utils.setFinalField(field, null, newblock);
				}
			}
		}
	}

}
