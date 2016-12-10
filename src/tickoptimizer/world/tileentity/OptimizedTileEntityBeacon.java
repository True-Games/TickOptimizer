package tickoptimizer.world.tileentity;

import java.util.List;

import net.minecraft.server.v1_11_R1.AchievementList;
import net.minecraft.server.v1_11_R1.AxisAlignedBB;
import net.minecraft.server.v1_11_R1.Block;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.Blocks;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.MobEffect;
import net.minecraft.server.v1_11_R1.TileEntityBeacon;

public class OptimizedTileEntityBeacon extends TileEntityBeacon {

	@Override
	public void n() {
		addEffects();
		checkStructure();
	}

	@SuppressWarnings("unchecked")
	private void addEffects() {
		if (this.levels > 0 && this.primaryEffect != null) {
			byte amplifier = 0;
			if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect) {
				amplifier = 1;
			}
			final int duration = (9 + this.levels * 2) * 20;
			final List<EntityHuman> list = getHumansInRange();
			for (final EntityHuman entityhuman : list) {
				entityhuman.addEffect(new MobEffect(this.primaryEffect, duration, amplifier, true, true));
			}
			if (this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect != null) {
				for (final EntityHuman entityhuman : list) {
					entityhuman.addEffect(new MobEffect(this.secondaryEffect, duration, 0, true, true));
				}
			}
		}
	}

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
					if (!isValidBlock(new BlockPosition(x, y, z))) {
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

	private boolean isValidBlock(BlockPosition blockpos) {
		if (this.world.isLoaded(blockpos)) {
			Block block = this.world.getType(blockpos).getBlock();
			if (block == Blocks.EMERALD_BLOCK || block == Blocks.GOLD_BLOCK || block == Blocks.DIAMOND_BLOCK || block == Blocks.IRON_BLOCK) {
				return true;
			}
		}
		return false;
	}

}
