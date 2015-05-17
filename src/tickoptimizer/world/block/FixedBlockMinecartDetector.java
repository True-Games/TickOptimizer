package tickoptimizer.world.block;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockMinecartDetector;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.Material;
import net.minecraft.server.v1_8_R3.World;

public class FixedBlockMinecartDetector extends BlockMinecartDetector {

	public FixedBlockMinecartDetector() {
		c(0.7f);
		a(Block.j);
		c("detectorRail");
	}

	@Override
	public void doPhysics(final World world, final BlockPosition air, final IBlockData blockData, final Block block) {
		final EnumTrackPosition enumTrackPosition = blockData.get(this.n());
		if (
			!World.a(world, air.down()) ||
			(enumTrackPosition == EnumTrackPosition.ASCENDING_EAST && !World.a(world, air.east())) ||
			(enumTrackPosition == EnumTrackPosition.ASCENDING_WEST && !World.a(world, air.west())) ||
			(enumTrackPosition == EnumTrackPosition.ASCENDING_NORTH && !World.a(world, air.north())) ||
			(enumTrackPosition == EnumTrackPosition.ASCENDING_SOUTH && !World.a(world, air.south()))
		) {
			if (world.getType(air).getBlock().getMaterial() != Material.AIR) {
				this.b(world, air, blockData, 0);
				world.setAir(air);
			}
		} else {
			this.b(world, air, blockData, block);
		}
	}

}
