package tickoptimizer.world.tileentity;

import java.util.List;

import net.minecraft.server.v1_8_R1.AchievementList;
import net.minecraft.server.v1_8_R1.AxisAlignedBB;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.MobEffect;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.TileEntityBeacon;

public class OptimizedTileEntityBeacon extends TileEntityBeacon {

	private int levels = 0;
	private int primary = 0;
	private int secondary = 0;

	@Override
	public void m() {
		addEffects();
		checkStructure();
	}

	@SuppressWarnings("unchecked")
	private void addEffects() {
		if (this.levels > 0 && this.primary > 0) {
			final double aoe = this.levels * 10 + 10;
			byte amplifier = 0;
			if (this.levels >= 4 && this.primary == this.secondary) {
				amplifier = 1;
			}
			final int i = this.position.getX();
			final int j = this.position.getY();
			final int k = this.position.getZ();
			final AxisAlignedBB axisalignedbb = new AxisAlignedBB(i, j, k, (i + 1), (j + 1), (k + 1)).grow(aoe, aoe, aoe).a(0.0, this.world.getHeight(), 0.0);
			final List<EntityHuman> list = this.world.a(EntityHuman.class, axisalignedbb);
			for (final EntityHuman entityhuman : list) {
				entityhuman.addEffect(new MobEffect(this.primary, 180, amplifier, true, true));
			}
			if (this.levels >= 4 && this.primary != this.secondary && this.secondary > 0) {
				for (final EntityHuman entityhuman : list) {
					entityhuman.addEffect(new MobEffect(this.secondary, 180, 0, true, true));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void checkStructure() {
		int prevLevels = this.levels;
		this.levels = 0;
		final int beaconX = this.position.getX();
		final int beaconY = this.position.getY();
		final int beaconZ = this.position.getZ();
		if (this.world.getHighestBlockYAt(this.position).getY() > beaconY) {
			return;
		}
		for (int level = 1; level <= 4; level++) {
			final int y = beaconY - level;
			if (y < 0) {
				break;
			}
			for (int x = beaconX - level; x <= beaconX + level; ++x) {
				for (int z = beaconZ - level; z <= beaconZ + level; ++z) {
					final Block block = this.world.getType(new BlockPosition(x, y, z)).getBlock();
					if (block != Blocks.EMERALD_BLOCK && block != Blocks.GOLD_BLOCK && block != Blocks.DIAMOND_BLOCK && block != Blocks.IRON_BLOCK) {
						return;
					}
				}
			}
			this.levels++;
		}
		if (this.levels == 4 && prevLevels < this.levels) {
			for (final EntityHuman entityhuman : (List<EntityHuman>) this.world.a(EntityHuman.class, new AxisAlignedBB((double) beaconX, (double) beaconY, (double) beaconZ, (double) beaconX, (double) (beaconY - 4), (double) beaconZ).grow(10.0, 5.0, 10.0))) {
				entityhuman.b(AchievementList.K);
			}
		}
	}

	@Override
	public int getProperty(final int key) {
		switch (key) {
			case 0: {
				return this.levels;
			}
			case 1: {
				return this.primary;
			}
			case 2: {
				return this.secondary;
			}
			default: {
				return 0;
			}
		}
	}

	@Override
	public void b(final int key, final int value) {
		switch (key) {
			case 0: {
				this.levels = value;
				break;
			}
			case 1: {
				this.primary = this.validateEffect(value);
				break;
			}
			case 2: {
				this.secondary = this.validateEffect(value);
				break;
			}
		}
	}

	@Override
	public void a(final NBTTagCompound nbttagcompound) {
		super.a(nbttagcompound);
		this.primary = this.validateEffect(nbttagcompound.getInt("Primary"));
		this.secondary = this.validateEffect(nbttagcompound.getInt("Secondary"));
		this.levels = nbttagcompound.getInt("Levels");
	}

	@Override
	public void b(final NBTTagCompound nbttagcompound) {
		super.b(nbttagcompound);
		nbttagcompound.setInt("Primary", this.primary);
		nbttagcompound.setInt("Secondary", this.secondary);
		nbttagcompound.setInt("Levels", this.levels);
	}

}
