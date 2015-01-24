package tickoptimizer.world.block;

import tickoptimizer.world.tileentity.OptimizedTileEntityBeacon;
import net.minecraft.server.v1_8_R1.BlockBeacon;
import net.minecraft.server.v1_8_R1.TileEntity;
import net.minecraft.server.v1_8_R1.World;

public class InjectTEBlockBeacon extends BlockBeacon {

	public InjectTEBlockBeacon() {
		c("beacon");
		a(1.0f);
	}

	@Override
	public TileEntity a(final World world, final int n) {
		return new OptimizedTileEntityBeacon();
	}

}
