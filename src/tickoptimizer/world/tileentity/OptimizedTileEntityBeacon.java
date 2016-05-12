package tickoptimizer.world.tileentity;

import java.util.List;

import net.minecraft.server.v1_9_R2.AchievementList;
import net.minecraft.server.v1_9_R2.AxisAlignedBB;
import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.Blocks;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.MobEffect;
import net.minecraft.server.v1_9_R2.MobEffectList;
import net.minecraft.server.v1_9_R2.MobEffects;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.TileEntityBeacon;

public class OptimizedTileEntityBeacon extends TileEntityBeacon {

	private int levels = 0;
	private MobEffectList primary;
	private MobEffectList secondary;

	@Override
	public void m() {
		addEffects();
		checkStructure();
	}

	private void addEffects() {
		if (this.levels > 0 && this.primary != null) {
			final double aoe = this.levels * 10 + 10;
			byte amplifier = 0;
			if (this.levels >= 4 && this.primary == this.secondary) {
				amplifier = 1;
			}
			final int duration = (9 + this.levels * 2) * 20;
			final int x = this.position.getX();
			final int y = this.position.getY();
			final int z = this.position.getZ();
			final AxisAlignedBB axisalignedbb = new AxisAlignedBB(x, y, z, (x + 1), (y + 1), (z + 1)).grow(aoe, aoe, aoe).a(0.0, this.world.getHeight(), 0.0);
			final List<EntityHuman> list = this.world.a(EntityHuman.class, axisalignedbb);
			for (final EntityHuman entityhuman : list) {
				entityhuman.addEffect(new MobEffect(this.primary, duration, amplifier, true, true));
			}
			if (this.levels >= 4 && this.primary != this.secondary && this.secondary != null) {
				for (final EntityHuman entityhuman : list) {
					entityhuman.addEffect(new MobEffect(this.secondary, duration, 0, true, true));
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

	@Override
	public int getProperty(final int key) {
		switch (key) {
			case 0: {
				return this.levels;
			}
			case 1: {
				return MobEffectList.getId(this.primary);
			}
			case 2: {
				return MobEffectList.getId(this.secondary);
			}
			default: {
				return 0;
			}
		}
	}

	@Override
	public void setProperty(final int key, final int value) {
		switch (key) {
			case 0: {
				this.levels = value;
				break;
			}
			case 1: {
				this.primary = this.getByIdAndValidate(value);
				break;
			}
			case 2: {
				this.secondary = this.getByIdAndValidate(value);
				break;
			}
		}
	}

	@Override
	public void a(final NBTTagCompound nbttagcompound) {
		super.a(nbttagcompound);
		this.primary = this.getByIdAndValidate(nbttagcompound.getInt("Primary"));
		this.secondary = this.getByIdAndValidate(nbttagcompound.getInt("Secondary"));
		this.levels = nbttagcompound.getInt("Levels");
	}

	@Override
	public NBTTagCompound save(final NBTTagCompound nbttagcompound) {
		super.save(nbttagcompound);
		nbttagcompound.setInt("Primary", MobEffectList.getId(this.primary));
		nbttagcompound.setInt("Secondary", MobEffectList.getId(this.secondary));
		nbttagcompound.setInt("Levels", this.levels);
		return nbttagcompound;
	}

	private MobEffectList getByIdAndValidate(int input) {
		MobEffectList effect = MobEffectList.fromId(input);
		if (
			effect == MobEffects.FASTER_MOVEMENT ||
			effect == MobEffects.FASTER_DIG ||
			effect == MobEffects.RESISTANCE ||
			effect == MobEffects.JUMP ||
			effect == MobEffects.INCREASE_DAMAGE ||
			effect == MobEffects.REGENERATION
		) {
			return effect;
		}
		return null;
	}

}
