package tickoptimizer.world.block;

import tickoptimizer.world.tileentity.OptimizedTileEntityBeacon;
import net.minecraft.server.v1_9_R2.BlockBeacon;
import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.World;

public class InjectTEBlockBeacon extends BlockBeacon {

	public InjectTEBlockBeacon() {
		c("beacon");
		a(1.0F);
	}

	@Override
	public TileEntity a(final World world, final int n) {
		return new OptimizedTileEntityBeacon();
	}

}
