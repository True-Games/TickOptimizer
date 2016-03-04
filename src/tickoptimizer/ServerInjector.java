package tickoptimizer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.Blocks;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.BlockDispenser;
import net.minecraft.server.v1_9_R1.EntityTypes;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.IDispenseBehavior;
import net.minecraft.server.v1_9_R1.Item;
import net.minecraft.server.v1_9_R1.ItemBlock;
import net.minecraft.server.v1_9_R1.ItemBucket;
import net.minecraft.server.v1_9_R1.Items;
import net.minecraft.server.v1_9_R1.Material;
import net.minecraft.server.v1_9_R1.MinecraftKey;
import net.minecraft.server.v1_9_R1.MinecraftServer;
import net.minecraft.server.v1_9_R1.RegistryDefault;
import net.minecraft.server.v1_9_R1.TileEntity;
import net.minecraft.server.v1_9_R1.UserCache;

import org.bukkit.Bukkit;

import tickoptimizer.usercache.OptimizedUserCache;
import tickoptimizer.utils.Utils;
import tickoptimizer.world.block.InjectTEBlockBeacon;
import tickoptimizer.world.block.InjectTEBlockEnderChest;
import tickoptimizer.world.block.InjectTEBlockNormalChest;
import tickoptimizer.world.block.InjectTEBlockTrappedChest;
import tickoptimizer.world.block.OptimizedBlockFlowing;
import tickoptimizer.world.tileentity.MovedSoundTileEntityChest;
import tickoptimizer.world.tileentity.OptimizedTileEntityBeacon;
import tickoptimizer.world.tileentity.OptimizedTileEntityEnderChest;

public class ServerInjector {

	@SuppressWarnings("deprecation")
	public static void injectUserCache() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		UserCache oldUserCache = MinecraftServer.getServer().getUserCache();
		File oldUserCacheFile = (File) Utils.setAccessible(oldUserCache.getClass().getDeclaredField("h")).get(oldUserCache);
		OptimizedUserCache newUserCache = new OptimizedUserCache(MinecraftServer.getServer().getGameProfileRepository(), oldUserCacheFile);
		Utils.setFinalField(MinecraftServer.class.getDeclaredField("X"), MinecraftServer.getServer(), newUserCache);
	}

	public static void injectRegistry() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		registerTileEntity("Beacon", OptimizedTileEntityBeacon.class);
		registerBlock(138, "beacon", new InjectTEBlockBeacon());

		registerTileEntity("Chest", MovedSoundTileEntityChest.class);
		registerBlock(54, "chest", new InjectTEBlockNormalChest());
		registerBlock(146, "trapped_chest", new InjectTEBlockTrappedChest());

		registerTileEntity("EnderChest", OptimizedTileEntityEnderChest.class);
		registerBlock(130, "ender_chest", new InjectTEBlockEnderChest());

		OptimizedBlockFlowing blockFlowingWater = new OptimizedBlockFlowing(Material.WATER, true);
		registerBlock(8, "flowing_water", blockFlowingWater);
		OptimizedBlockFlowing blockFlowingLava = new OptimizedBlockFlowing(Material.LAVA, false);
		registerBlock(10, "flowing_lava", blockFlowingLava);

		fixItemBucketRef("water_bucket", blockFlowingWater);
		fixItemBucketRef("lava_bucket", blockFlowingLava);

		fixBlocksRefs();
		fixItemsRefs();
		fixDispenserRegistry();

		Bukkit.resetRecipes();
	}

	private static void fixItemBucketRef(String itemname, Block newBlockRef) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Utils.setAccessible(ItemBucket.class.getDeclaredField("a")).set(Item.d(itemname), newBlockRef);
	}


	@SuppressWarnings("unchecked")
	private static void registerTileEntity(String name, Class<? extends TileEntity> entityClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		((Map<String, Class<? extends TileEntity>>) Utils.setAccessible(TileEntity.class.getDeclaredField("f")).get(null)).put(name, entityClass);
		((Map<Class<? extends TileEntity>, String>) Utils.setAccessible(TileEntity.class.getDeclaredField("g")).get(null)).put(entityClass, name);
	}

	@SuppressWarnings("unchecked")
	private static void registerBlock(int id, String name, Block block) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		MinecraftKey stringkey = new MinecraftKey(name);
		ItemBlock itemblock = new ItemBlock(block);
		Block.REGISTRY.a(id, stringkey, block);
		Iterator<IBlockData> blockdataiterator = block.t().a().iterator();
		while (blockdataiterator.hasNext()) {
			IBlockData blockdata = blockdataiterator.next();
			final int stateId = (id << 4) | block.toLegacyData(blockdata);
			Block.REGISTRY_ID.a(blockdata, stateId);
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

	private static final void fixDispenserRegistry() {
		RegistryDefault<Item, IDispenseBehavior> registry = BlockDispenser.REGISTRY;
		for (Item item : new ArrayList<Item>(registry.keySet())) {
			Item newItem = Item.getById(Item.getId(item));
			if (item != newItem) {
				registry.a(newItem, registry.get(item));
			}
		}
	}

}
