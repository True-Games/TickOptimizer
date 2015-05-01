package tickoptimizer.world.block;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import net.minecraft.server.v1_8_R2.BlockDoor;
import net.minecraft.server.v1_8_R2.BlockFlowing;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.EnumDirection.EnumDirectionLimit;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.Material;
import net.minecraft.server.v1_8_R2.World;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R2.CraftServer;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.block.CraftBlock;
import org.bukkit.event.block.BlockFromToEvent;

public class OptimizedBlockFlowing extends BlockFlowing {

	int a;

	public OptimizedBlockFlowing(Material material, boolean isWater) {
		super(material);
		c(100.0F);
		K();
		if (isWater) {
			e(3);
			c("water");
		} else {
			a(1.0F);
			c("lava");
		}
	}

	private void setLiquid(World world, BlockPosition blockposition, IBlockData iblockdata) {
		world.setTypeAndData(blockposition, b(material).getBlockData().set(LEVEL, iblockdata.get(LEVEL)), 2);
	}

	@Override
	public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
		CraftWorld bworld = world.getWorld();
		CraftServer server = world.getServer();
		Block source = bworld == null ? null : bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
		int currentlevel = iblockdata.get(LEVEL).intValue();
		byte b0 = 1;
		if ((material == Material.LAVA) && !world.worldProvider.n()) {
			b0 = 2;
		}

		int j = this.a(world);
		int k;
		if (currentlevel > 0) {
			int iblockdata2 = -100;
			a = 0;

			EnumDirection set;
			for (Iterator<EnumDirection> iterator1 = EnumDirectionLimit.HORIZONTAL.iterator(); iterator1.hasNext(); iblockdata2 = this.a(world, blockposition.shift(set), iblockdata2)) {
				set = iterator1.next();
			}

			int newlevel = iblockdata2 + b0;
			if ((newlevel >= 8) || (iblockdata2 < 0)) {
				newlevel = -1;
			}

			if (this.e(world, blockposition.up()) >= 0) {
				k = this.e(world, blockposition.up());
				if (k >= 8) {
					newlevel = k;
				} else {
					newlevel = k + 8;
				}
			}

			if ((a >= 2) && (material == Material.WATER)) {
				IBlockData enumdirection1 = world.getType(blockposition.down());
				if (enumdirection1.getBlock().getMaterial().isBuildable()) {
					newlevel = 0;
				} else if ((enumdirection1.getBlock().getMaterial() == material) && (enumdirection1.get(LEVEL).intValue() == 0)) {
					newlevel = 0;
				}
			}

			if ((material == Material.LAVA) && (currentlevel < 8) && (newlevel < 8) && (newlevel > currentlevel) && (random.nextInt(4) != 0)) {
				j *= 4;
			}

			if (newlevel == currentlevel) {
				this.setLiquid(world, blockposition, iblockdata);
			} else {
				currentlevel = newlevel;
				if (newlevel < 0) {
					world.setAir(blockposition);
				} else {
					iblockdata = iblockdata.set(LEVEL, Integer.valueOf(newlevel));
					world.setTypeAndData(blockposition, iblockdata, 2);
					world.a(blockposition, this, j);
					world.d(blockposition.west(), this);
					world.d(blockposition.east(), this);
					world.d(blockposition.up(), this);
					world.d(blockposition.north(), this);
					world.d(blockposition.south(), this);
				}
			}
		} else {
			this.setLiquid(world, blockposition, iblockdata);
		}

		IBlockData iblockdata21 = world.getType(blockposition.down());
		if (this.h(world, blockposition.down(), iblockdata21)) {
			BlockFromToEvent set1 = new BlockFromToEvent(source, BlockFace.DOWN);
			if (server != null) {
				server.getPluginManager().callEvent(set1);
			}

			if (!set1.isCancelled()) {
				if ((material == Material.LAVA) && (world.getType(blockposition.down()).getBlock().getMaterial() == Material.WATER)) {
					world.setTypeUpdate(blockposition.down(), Blocks.STONE.getBlockData());
					fizz(world, blockposition.down());
					return;
				}

				if (currentlevel >= 8) {
					flow(world, blockposition.down(), iblockdata21, currentlevel);
				} else {
					flow(world, blockposition.down(), iblockdata21, currentlevel + 8);
				}
			}
		} else if ((currentlevel >= 0) && ((currentlevel == 0) || this.g(world, blockposition.down(), iblockdata21))) {
			Set<EnumDirection> flowdirections = this.getFlowDirections(world, blockposition);
			k = currentlevel + b0;
			if (currentlevel >= 8) {
				k = 1;
			}

			if (k >= 8) {
				return;
			}

			for (EnumDirection flowdirection : flowdirections) {
				BlockFromToEvent event = new BlockFromToEvent(source, CraftBlock.notchToBlockFace(flowdirection));
				if (server != null) {
					server.getPluginManager().callEvent(event);
				}

				if (!event.isCancelled()) {
					flow(world, blockposition.shift(flowdirection), world.getType(blockposition.shift(flowdirection)), k);
				}
			}
		}
	}

	private void flow(World world, BlockPosition blockposition, IBlockData iblockdata, int i) {
		if (world.isLoaded(blockposition) && this.h(world, blockposition, iblockdata)) {
			if (iblockdata.getBlock() != Blocks.AIR) {
				if (material == Material.LAVA) {
					fizz(world, blockposition);
				} else {
					iblockdata.getBlock().b(world, blockposition, iblockdata, 0);
				}
			}

			world.setTypeAndData(blockposition, getBlockData().set(LEVEL, Integer.valueOf(i)), 3);
		}
	}

	private int a(World world, BlockPosition blockposition, int i, EnumDirection enumdirection) {
		int j = 1000;
		Iterator<?> iterator = EnumDirectionLimit.HORIZONTAL.iterator();

		while (iterator.hasNext()) {
			EnumDirection enumdirection1 = (EnumDirection) iterator.next();
			if (enumdirection1 != enumdirection) {
				BlockPosition blockposition1 = blockposition.shift(enumdirection1);
				IBlockData iblockdata = world.getType(blockposition1);
				if (!this.g(world, blockposition1, iblockdata) && ((iblockdata.getBlock().getMaterial() != material) || (iblockdata.get(LEVEL).intValue() > 0))) {
					if (!this.g(world, blockposition1.down(), iblockdata)) {
						return i;
					}

					if (i < 4) {
						int k = this.a(world, blockposition1, i + 1, enumdirection1.opposite());
						if (k < j) {
							j = k;
						}
					}
				}
			}
		}

		return j;
	}

	private Set<EnumDirection> getFlowDirections(World world, BlockPosition blockposition) {
		int i = 1000;
		EnumSet<EnumDirection> enumset = EnumSet.noneOf(EnumDirection.class);
		Iterator<EnumDirection> iterator = EnumDirectionLimit.HORIZONTAL.iterator();

		while (iterator.hasNext()) {
			EnumDirection enumdirection = iterator.next();
			BlockPosition blockposition1 = blockposition.shift(enumdirection);
			IBlockData iblockdata = world.getType(blockposition1);
			if (!this.g(world, blockposition1, iblockdata) && ((iblockdata.getBlock().getMaterial() != material) || (iblockdata.get(LEVEL).intValue() > 0))) {
				int j;
				if (this.g(world, blockposition1.down(), world.getType(blockposition1.down()))) {
					j = this.a(world, blockposition1, 1, enumdirection.opposite());
				} else {
					j = 0;
				}

				if (j < i) {
					enumset.clear();
				}

				if (j <= i) {
					enumset.add(enumdirection);
					i = j;
				}
			}
		}

		return enumset;
	}

	private boolean g(World world, BlockPosition blockposition, IBlockData iblockdata) {
		net.minecraft.server.v1_8_R2.Block block = world.getType(blockposition).getBlock();
		return !(block instanceof BlockDoor) && (block != Blocks.STANDING_SIGN) && (block != Blocks.LADDER) && (block != Blocks.REEDS) ? (block.getMaterial() == Material.PORTAL ? true : block.getMaterial().isSolid()) : true;
	}

	@Override
	protected int a(World world, BlockPosition blockposition, int i) {
		int j = this.e(world, blockposition);
		if (j < 0) {
			return i;
		} else {
			if (j == 0) {
				++a;
			}

			if (j >= 8) {
				j = 0;
			}

			return (i >= 0) && (j >= i) ? i : j;
		}
	}

	private boolean h(World world, BlockPosition blockposition, IBlockData iblockdata) {
		Material material = iblockdata.getBlock().getMaterial();
		return (material != this.material) && (material != Material.LAVA) && !this.g(world, blockposition, iblockdata);
	}

}
