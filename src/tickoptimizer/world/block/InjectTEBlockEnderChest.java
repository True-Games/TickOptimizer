package tickoptimizer.world.block;

import tickoptimizer.world.tileentity.OptimizedTileEntityEnderChest;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockEnderChest;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.World;

public class InjectTEBlockEnderChest extends BlockEnderChest {

	public InjectTEBlockEnderChest() {
		c(22.5f);
		b(1000.0f);
		a(Block.i);
		c("enderChest");
		a(0.5f);
	}

	@Override
	public TileEntity a(final World world, final int n) {
		return new OptimizedTileEntityEnderChest();
	}

}
