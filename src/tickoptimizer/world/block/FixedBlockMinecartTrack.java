package tickoptimizer.world.block;

import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockMinecartTrack;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EnumTrackPosition;
import net.minecraft.server.v1_8_R1.IBlockData;
import net.minecraft.server.v1_8_R1.Material;
import net.minecraft.server.v1_8_R1.World;

public class FixedBlockMinecartTrack extends BlockMinecartTrack {

	public FixedBlockMinecartTrack() {
		c(0.7f);
		a(Block.j);
		c("rail");
	}

	@Override
	public void doPhysics(final World world, final BlockPosition air, final IBlockData blockData, final Block block) {
		final EnumTrackPosition enumTrackPosition = (EnumTrackPosition) blockData.get(this.l());
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
