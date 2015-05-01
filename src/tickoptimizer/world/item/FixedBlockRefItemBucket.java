package tickoptimizer.world.item;

import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockFluids;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.Item;
import net.minecraft.server.v1_8_R2.ItemBucket;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.Items;
import net.minecraft.server.v1_8_R2.Material;
import net.minecraft.server.v1_8_R2.MovingObjectPosition;
import net.minecraft.server.v1_8_R2.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.server.v1_8_R2.StatisticList;
import net.minecraft.server.v1_8_R2.World;

import org.bukkit.craftbukkit.v1_8_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class FixedBlockRefItemBucket extends ItemBucket {

	private Block block;

	public FixedBlockRefItemBucket(Block block, boolean isWater) {
		super(block);
		this.block = block;
		c(Items.BUCKET);
		if (isWater) {
			c("bucketWater");
		} else {
			c("bucketLava");
		}
	}

	@Override
	public ItemStack a(ItemStack itemstack, World world, EntityHuman entityhuman) {
		boolean isAir = block == Blocks.AIR;
		MovingObjectPosition movingobjectposition = this.a(world, entityhuman, isAir);
		if (movingobjectposition == null) {
			return itemstack;
		} else {
			if (movingobjectposition.type == EnumMovingObjectType.BLOCK) {
				BlockPosition blockposition = movingobjectposition.a();
				if (!world.a(entityhuman, blockposition)) {
					return itemstack;
				}

				if (isAir) {
					if (!entityhuman.a(blockposition.shift(movingobjectposition.direction), movingobjectposition.direction, itemstack)) {
						return itemstack;
					}

					IBlockData blockposition1 = world.getType(blockposition);
					Material event = blockposition1.getBlock().getMaterial();
					PlayerBucketFillEvent event1;
					if ((event == Material.WATER) && (blockposition1.get(BlockFluids.LEVEL).intValue() == 0)) {
						event1 = CraftEventFactory.callPlayerBucketFillEvent(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), (EnumDirection) null, itemstack, Items.WATER_BUCKET);
						if (event1.isCancelled()) {
							return itemstack;
						}

						world.setAir(blockposition);
						entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this)]);
						return this.a(itemstack, entityhuman, Items.WATER_BUCKET, event1.getItemStack());
					}

					if ((event == Material.LAVA) && (blockposition1.get(BlockFluids.LEVEL).intValue() == 0)) {
						event1 = CraftEventFactory.callPlayerBucketFillEvent(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), (EnumDirection) null, itemstack, Items.LAVA_BUCKET);
						if (event1.isCancelled()) {
							return itemstack;
						}

						world.setAir(blockposition);
						entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this)]);
						return this.a(itemstack, entityhuman, Items.LAVA_BUCKET, event1.getItemStack());
					}
				} else {
					if (block == Blocks.AIR) {
						PlayerBucketEmptyEvent blockposition12 = CraftEventFactory.callPlayerBucketEmptyEvent(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), movingobjectposition.direction, itemstack);
						if (blockposition12.isCancelled()) {
							return itemstack;
						}

						return CraftItemStack.asNMSCopy(blockposition12.getItemStack());
					}

					BlockPosition blockposition11 = blockposition.shift(movingobjectposition.direction);
					if (!entityhuman.a(blockposition11, movingobjectposition.direction, itemstack)) {
						return itemstack;
					}

					PlayerBucketEmptyEvent event2 = CraftEventFactory.callPlayerBucketEmptyEvent(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), movingobjectposition.direction, itemstack);
					if (event2.isCancelled()) {
						return itemstack;
					}

					if (this.a(world, blockposition11) && !entityhuman.abilities.canInstantlyBuild) {
						entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this)]);
						return CraftItemStack.asNMSCopy(event2.getItemStack());
					}
				}
			}

			return itemstack;
		}
	}

	private ItemStack a(ItemStack itemstack, EntityHuman entityhuman, Item item, org.bukkit.inventory.ItemStack result) {
		if (entityhuman.abilities.canInstantlyBuild) {
			return itemstack;
		} else if (--itemstack.count <= 0) {
			return CraftItemStack.asNMSCopy(result);
		} else {
			if (!entityhuman.inventory.pickup(CraftItemStack.asNMSCopy(result))) {
				entityhuman.drop(CraftItemStack.asNMSCopy(result), false);
			}
			return itemstack;
		}
	}

	@Override
	public boolean a(World world, BlockPosition blockposition) {
		if (block == Blocks.AIR) {
			return false;
		} else {
			Material material = world.getType(blockposition).getBlock().getMaterial();
			boolean flag = !material.isBuildable();
			if (!world.isEmpty(blockposition) && !flag) {
				return false;
			} else {
				if (world.worldProvider.n() && (block == Blocks.FLOWING_WATER)) {
					int x = blockposition.getX();
					int y = blockposition.getY();
					int z = blockposition.getZ();
					world.makeSound(x + 0.5F, y + 0.5F, z + 0.5F, "random.fizz", 0.5F, 2.6F + ((world.random.nextFloat() - world.random.nextFloat()) * 0.8F));

					for (int l = 0; l < 8; ++l) {
						world.addParticle(EnumParticle.SMOKE_LARGE, x + Math.random(), y + Math.random(), z + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]);
					}
				} else {
					if (flag && !material.isLiquid()) {
						world.setAir(blockposition, true);
					}

					world.setTypeAndData(blockposition, block.getBlockData(), 3);
				}

				return true;
			}
		}
	}

}
