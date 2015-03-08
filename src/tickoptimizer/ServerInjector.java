package tickoptimizer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityTypes;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.Item;
import net.minecraft.server.v1_8_R2.ItemBlock;
import net.minecraft.server.v1_8_R2.Items;
import net.minecraft.server.v1_8_R2.MinecraftKey;
import net.minecraft.server.v1_8_R2.MinecraftServer;
import net.minecraft.server.v1_8_R2.TileEntity;
import net.minecraft.server.v1_8_R2.UserCache;

import org.bukkit.Bukkit;

import tickoptimizer.usercache.OptimizedUserCache;
import tickoptimizer.utils.Utils;
import tickoptimizer.world.block.FixedBlockMinecartDetector;
import tickoptimizer.world.block.FixedBlockMinecartTrack;
import tickoptimizer.world.block.FixedBlockPoweredRail;
import tickoptimizer.world.block.InjectTEBlockBeacon;
import tickoptimizer.world.block.InjectTEBlockEnderChest;
import tickoptimizer.world.block.InjectTEBlockNormalChest;
import tickoptimizer.world.block.InjectTEBlockTrappedChest;
import tickoptimizer.world.entity.OptimizedEntityItemFrame;
import tickoptimizer.world.item.InjectEntityItemFrame;
import tickoptimizer.world.tileentity.MovedSoundTileEntityChest;
import tickoptimizer.world.tileentity.OptimizedTileEntityBeacon;
import tickoptimizer.world.tileentity.OptimizedTileEntityEnderChest;

public class ServerInjector {

	public static void injectUserCache() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		UserCache oldUserCache = MinecraftServer.getServer().getUserCache();
		File oldUserCacheFile = (File) Utils.setAccessible(oldUserCache.getClass().getDeclaredField("g")).get(oldUserCache);
		OptimizedUserCache newUserCache = new OptimizedUserCache(MinecraftServer.getServer(), oldUserCacheFile);
		Utils.setFinalField(MinecraftServer.class.getDeclaredField("Z"), MinecraftServer.getServer(), newUserCache);
	}

	public static void injectRegistry() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		registerTileEntity("Beacon", OptimizedTileEntityBeacon.class);
		registerBlock(138, "beacon", new InjectTEBlockBeacon());

		registerTileEntity("Chest", MovedSoundTileEntityChest.class);
		registerBlock(54, "chest", new InjectTEBlockNormalChest());
		registerBlock(146, "trapped_chest", new InjectTEBlockTrappedChest());

		registerTileEntity("EnderChest", OptimizedTileEntityEnderChest.class);
		registerBlock(130, "ender_chest", new InjectTEBlockEnderChest());

		registerBlock(27, "golden_rail", new FixedBlockPoweredRail());
		registerBlock(28, "detector_rail", new FixedBlockMinecartDetector());
		registerBlock(66, "rail", new FixedBlockMinecartTrack());

		registerEntity(18, "ItemFrame", OptimizedEntityItemFrame.class);
		registerItem(389, "item_frame", new InjectEntityItemFrame());

		fixBlocksRefs();
		fixItemsRefs();
		Bukkit.resetRecipes();
	}


	@SuppressWarnings("unchecked")
	private static void registerTileEntity(String name, Class<? extends TileEntity> entityClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		((Map<String, Class<? extends TileEntity>>) Utils.setAccessible(TileEntity.class.getDeclaredField("f")).get(null)).put(name, entityClass);
		((Map<Class<? extends TileEntity>, String>) Utils.setAccessible(TileEntity.class.getDeclaredField("g")).get(null)).put(entityClass, name);
	}

	private static void registerItem(int id, String name, Item item) {
		MinecraftKey stringkey = new MinecraftKey(name);
		Item.REGISTRY.a(id, stringkey, item);
	}

	@SuppressWarnings("unchecked")
	private static void registerBlock(int id, String name, Block block) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		MinecraftKey stringkey = new MinecraftKey(name);
		ItemBlock itemblock = new ItemBlock(block);
		Block.REGISTRY.a(id, stringkey, block);
		Iterator<IBlockData> blockdataiterator = block.P().a().iterator();
		while (blockdataiterator.hasNext()) {
			IBlockData blockdata = blockdataiterator.next();
			final int stateId = (id << 4) | block.toLegacyData(blockdata);
			Block.d.a(blockdata, stateId);
		}
		Item.REGISTRY.a(id, stringkey, itemblock);
		((Map<Block, Item>)Utils.setAccessible(Item.class.getDeclaredField("a")).get(null)).put(block, itemblock);
	}

	@SuppressWarnings("unchecked")
	public static void registerEntity(int id, String name, Class<? extends Entity> entityClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		((Map<String, Class<? extends Entity>>) Utils.setAccessible(EntityTypes.class.getDeclaredField("c")).get(null)).put(name, entityClass);
		((Map<Class<? extends Entity>, String>) Utils.setAccessible(EntityTypes.class.getDeclaredField("d")).get(null)).put(entityClass, name);
		((Map<Integer, Class<? extends Entity>>) Utils.setAccessible(EntityTypes.class.getDeclaredField("e")).get(null)).put(id, entityClass);
		((Map<Class<? extends Entity>, Integer>) Utils.setAccessible(EntityTypes.class.getDeclaredField("f")).get(null)).put(entityClass, id);
		((Map<String, Integer>) Utils.setAccessible(EntityTypes.class.getDeclaredField("g")).get(null)).put(name, id);
	}

	private static void fixBlocksRefs() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		for (Field field : Blocks.class.getDeclaredFields()) {
			field.setAccessible(true);
			if (Block.class.isAssignableFrom(field.getType())) {
				Block block = (Block) field.get(null);
				Block newblock = Block.getById(Block.getId(block));
				if (block != newblock) {
					Utils.setFinalField(field, null, newblock);
				}
			}
		}
	}

	private static void fixItemsRefs() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		for (Field field : Items.class.getDeclaredFields()) {
			field.setAccessible(true);
			if (Item.class.isAssignableFrom(field.getType())) {
				Item item = (Item) field.get(null);
				Item newitem = Item.getById(Item.getId(item));
				if (item != newitem) {
					Utils.setFinalField(field, null, newitem);
				}
			}
		}
	}

}
