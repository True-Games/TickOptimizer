package tickoptimizer.world.entity;

import java.util.List;

import net.minecraft.server.v1_8_R1.AxisAlignedBB;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockDiodeAbstract;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.Entity;
import net.minecraft.server.v1_8_R1.EntityHanging;
import net.minecraft.server.v1_8_R1.EntityItemFrame;
import net.minecraft.server.v1_8_R1.EnumDirection;
import net.minecraft.server.v1_8_R1.World;

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
				blockPosition.getX() + blockAt.z(),
				blockPosition.getY() + blockAt.B(),
				blockPosition.getZ() + blockAt.D(),
				blockPosition.getX() + blockAt.A(),
				blockPosition.getY() + blockAt.C(),
				blockPosition.getZ() + blockAt.E()
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
		@SuppressWarnings("unchecked")
		List<Entity> entityList = this.world.getEntities(this, this.getBoundingBox());
		for (Entity entity : entityList) {
			if (entity instanceof EntityHanging) {
				return false;
			}
		}
		return true;
	}

}
