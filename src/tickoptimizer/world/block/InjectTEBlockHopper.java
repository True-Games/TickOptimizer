package tickoptimizer.world.block;

import tickoptimizer.world.tileentity.OptimizedTileEntityHopper;
import net.minecraft.server.v1_8_R2.BlockHopper;
import net.minecraft.server.v1_8_R2.TileEntity;
import net.minecraft.server.v1_8_R2.World;

public class InjectTEBlockHopper extends BlockHopper {

	public InjectTEBlockHopper() {
		c(3.0F);
		b(8.0F);
		a(j);
		c("hopper");
	}

	@Override
	public TileEntity a(final World world, final int n) {
		return new OptimizedTileEntityHopper();
	}

}
