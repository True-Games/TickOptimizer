package tickoptimizer.world.entity;

import net.minecraft.server.v1_8_R2.AxisAlignedBB;
import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockDiodeAbstract;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityHanging;
import net.minecraft.server.v1_8_R2.EntityItemFrame;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.World;

public class OptimizedEntityItemFrame extends EntityItemFrame {

	public OptimizedEntityItemFrame(final World world) {
		super(world);
	}

	public OptimizedEntityItemFrame(final World world, final BlockPosition blockposition, EnumDirection enumdirection) {
		super(world, blockposition, enumdirection);
	}

	@Override
	public boolean survives() {
		Block holdingAt = world.getType(this.blockPosition.shift(this.direction.opposite())).getBlock();
		if (!holdingAt.getMaterial().isBuildable() && !BlockDiodeAbstract.d(holdingAt)) {
			return false;
		}
		Block blockAt = world.getType(blockPosition).getBlock();
		if (blockAt != Blocks.AIR) {
			AxisAlignedBB blockbounds = AxisAlignedBB.a(
				blockPosition.getX() + blockAt.B(),
				blockPosition.getY() + blockAt.D(),
				blockPosition.getZ() + blockAt.F(),
				blockPosition.getX() + blockAt.C(),
				blockPosition.getY() + blockAt.E(),
				blockPosition.getZ() + blockAt.G()
			);
			if (getBoundingBox().b(blockbounds)) {
				return false;
			}
		}
		return true;
	}

	public boolean canPlaceAt() {
		if (!survives()) {
			return false;
		}
		for (Entity entity : this.world.getEntities(this, this.getBoundingBox())) {
			if (entity instanceof EntityHanging) {
				return false;
			}
		}
		return true;
	}

}
