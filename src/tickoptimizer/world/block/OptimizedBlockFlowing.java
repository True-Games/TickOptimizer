package tickoptimizer.world.block;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftBlock;
import org.bukkit.event.block.BlockFromToEvent;

import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.BlockDoor;
import net.minecraft.server.v1_9_R1.BlockFlowing;
import net.minecraft.server.v1_9_R1.BlockFluids;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Blocks;
import net.minecraft.server.v1_9_R1.EnumDirection;
import net.minecraft.server.v1_9_R1.IBlockAccess;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.Material;
import net.minecraft.server.v1_9_R1.World;

public class OptimizedBlockFlowing extends BlockFlowing {

	int a;

	public OptimizedBlockFlowing(final Material material, boolean isWater) {
		super(material);
		c(100.0f);
		q();
		if (isWater) {
			d(3);
			c("water");
		} else {
			a(1.0f);
			c("lava");
		}
	}

	private void f(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
		world.setTypeAndData(blockposition, b(this.material).getBlockData().set(BlockFluids.LEVEL, iblockdata.get(BlockFluids.LEVEL)), 2);
	}

	@Override
	public void b(final World world, final BlockPosition blockposition, IBlockData iblockdata, final Random random) {
		final org.bukkit.World bworld = world.getWorld();
		final Server server = world.getServer();
		final org.bukkit.block.Block source = (bworld == null) ? null : bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
		int i = (int) iblockdata.get(BlockFluids.LEVEL);
		byte b0 = 1;
		if (this.material == Material.LAVA && !world.worldProvider.l()) {
			b0 = 2;
		}
		int j = this.a(world);
		if (i > 0) {
			int l = -100;
			this.a = 0;
			for (final EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
				l = this.a(world, blockposition.shift(enumdirection), l);
			}
			int i2 = l + b0;
			if (i2 >= 8 || l < 0) {
				i2 = -1;
			}
			if (this.c((IBlockAccess) world, blockposition.up()) >= 0) {
				final int k = this.c((IBlockAccess) world, blockposition.up());
				if (k >= 8) {
					i2 = k;
				} else {
					i2 = k + 8;
				}
			}
			if (this.a >= 2 && this.material == Material.WATER) {
				final IBlockData iblockdata2 = world.getType(blockposition.down());
				if (iblockdata2.getMaterial().isBuildable()) {
					i2 = 0;
				} else if (iblockdata2.getMaterial() == this.material && (int) iblockdata2.get(BlockFluids.LEVEL) == 0) {
					i2 = 0;
				}
			}
			if (this.material == Material.LAVA && i < 8 && i2 < 8 && i2 > i && random.nextInt(4) != 0) {
				j *= 4;
			}
			if (i2 == i) {
				this.f(world, blockposition, iblockdata);
			} else if ((i = i2) < 0) {
				world.setAir(blockposition);
			} else {
				iblockdata = iblockdata.set(BlockFluids.LEVEL, i2);
				world.setTypeAndData(blockposition, iblockdata, 2);
				world.a(blockposition, this, j);
				world.e(blockposition.west(), this);
				world.e(blockposition.east(), this);
				world.e(blockposition.up(), this);
				world.e(blockposition.north(), this);
				world.e(blockposition.south(), this);
			}
		} else {
			this.f(world, blockposition, iblockdata);
		}
		if (world.getType(blockposition).getBlock().getBlockData().getMaterial() != material) {
			return;
		}
		final IBlockData iblockdata3 = world.getType(blockposition.down());
		if (this.h(world, blockposition.down(), iblockdata3)) {
			final BlockFromToEvent event = new BlockFromToEvent(source, BlockFace.DOWN);
			if (server != null) {
				server.getPluginManager().callEvent(event);
			}
			if (!event.isCancelled()) {
				if (this.material == Material.LAVA && world.getType(blockposition.down()).getMaterial() == Material.WATER) {
					world.setTypeUpdate(blockposition.down(), Blocks.STONE.getBlockData());
					this.fizz(world, blockposition.down());
					return;
				}
				if (i >= 8) {
					this.flow(world, blockposition.down(), iblockdata3, i);
				} else {
					this.flow(world, blockposition.down(), iblockdata3, i + 8);
				}
			}
		} else if (i >= 0 && (i == 0 || this.g(world, blockposition.down(), iblockdata3))) {
			final Set<EnumDirection> set = this.c(world, blockposition);
			int k = i + b0;
			if (i >= 8) {
				k = 1;
			}
			if (k >= 8) {
				return;
			}
			for (final EnumDirection enumdirection2 : set) {
				final BlockFromToEvent event2 = new BlockFromToEvent(source, CraftBlock.notchToBlockFace(enumdirection2));
				if (server != null) {
					server.getPluginManager().callEvent(event2);
				}
				if (!event2.isCancelled()) {
					this.flow(world, blockposition.shift(enumdirection2), world.getType(blockposition.shift(enumdirection2)), k);
				}
			}
		}
	}

	private void flow(final World world, final BlockPosition blockposition, final IBlockData iblockdata, final int i) {
		if (world.isLoaded(blockposition) && this.h(world, blockposition, iblockdata)) {
			if (iblockdata.getBlock() != Blocks.AIR) {
				if (this.material == Material.LAVA) {
					this.fizz(world, blockposition);
				} else {
					iblockdata.getBlock().b(world, blockposition, iblockdata, 0);
				}
			}
			world.setTypeAndData(blockposition, this.getBlockData().set(BlockFluids.LEVEL, i), 3);
		}
	}

	private int a(final World world, final BlockPosition blockposition, final int i, final EnumDirection enumdirection) {
		int j = 1000;
		for (final EnumDirection enumdirection2 : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
			if (enumdirection2 != enumdirection) {
				final BlockPosition blockposition2 = blockposition.shift(enumdirection2);
				final IBlockData iblockdata = world.getType(blockposition2);
				if (this.g(world, blockposition2, iblockdata) || (iblockdata.getMaterial() == this.material && iblockdata.get(BlockFluids.LEVEL) <= 0)) {
					continue;
				}
				if (!this.g(world, blockposition2.down(), iblockdata)) {
					return i;
				}
				if (i >= this.b(world)) {
					continue;
				}
				final int k = this.a(world, blockposition2, i + 1, enumdirection2.opposite());
				if (k >= j) {
					continue;
				}
				j = k;
			}
		}
		return j;
	}

	private int b(final World world) {
		return (this.material == Material.LAVA && !world.worldProvider.l()) ? 2 : 4;
	}

	private Set<EnumDirection> c(final World world, final BlockPosition blockposition) {
		int i = 1000;
		final EnumSet<EnumDirection> enumset = EnumSet.noneOf(EnumDirection.class);
		for (final EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
			final BlockPosition blockposition2 = blockposition.shift(enumdirection);
			final IBlockData iblockdata = world.getType(blockposition2);
			if (!this.g(world, blockposition2, iblockdata) && (iblockdata.getMaterial() != this.material || (int) iblockdata.get(BlockFluids.LEVEL) > 0)) {
				int j;
				if (this.g(world, blockposition2.down(), world.getType(blockposition2.down()))) {
					j = this.a(world, blockposition2, 1, enumdirection.opposite());
				} else {
					j = 0;
				}
				if (j < i) {
					enumset.clear();
				}
				if (j > i) {
					continue;
				}
				enumset.add(enumdirection);
				i = j;
			}
		}
		return enumset;
	}

	private boolean g(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
		final Block block = world.getType(blockposition).getBlock();
		return
			block instanceof BlockDoor ||
			block == Blocks.STANDING_SIGN ||
			block == Blocks.LADDER ||
			block == Blocks.REEDS ||
			block.q(block.getBlockData()) == Material.PORTAL ||
			block.q(block.getBlockData()).isSolid();
	}

	@Override
	protected int a(final World world, final BlockPosition blockposition, final int i) {
		int j = this.c((IBlockAccess) world, blockposition);
		if (j < 0) {
			return i;
		}
		if (j == 0) {
			++this.a;
		}
		if (j >= 8) {
			j = 0;
		}
		return (i >= 0 && j >= i) ? i : j;
	}

	private boolean h(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
		final Material material = iblockdata.getMaterial();
		return material != this.material && material != Material.LAVA && !this.g(world, blockposition, iblockdata);
	}

	@Override
	public void onPlace(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
		if (!this.e(world, blockposition, iblockdata)) {
			world.a(blockposition, this, this.a(world));
		}
	}

}
