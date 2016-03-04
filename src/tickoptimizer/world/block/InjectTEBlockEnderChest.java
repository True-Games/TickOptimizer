package tickoptimizer.world.block;

import tickoptimizer.world.tileentity.OptimizedTileEntityEnderChest;
import net.minecraft.server.v1_9_R1.BlockEnderChest;
import net.minecraft.server.v1_9_R1.SoundEffectType;
import net.minecraft.server.v1_9_R1.TileEntity;
import net.minecraft.server.v1_9_R1.World;

public class InjectTEBlockEnderChest extends BlockEnderChest {

	public InjectTEBlockEnderChest() {
		c(22.5F);
		b(1000.0F);
		a(SoundEffectType.d);
		c("enderChest");
		a(0.5F);
	}

	@Override
	public TileEntity a(final World world, final int n) {
		return new OptimizedTileEntityEnderChest();
	}

}
