package tickoptimizer.world.block;

import tickoptimizer.world.tileentity.MovedSoundTileEntityChest;
import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockChest;
import net.minecraft.server.v1_8_R2.TileEntity;
import net.minecraft.server.v1_8_R2.World;

public abstract class InjectTEBlockChest extends BlockChest {

	protected InjectTEBlockChest(int i) {
		super(i);
		c(2.5f);
		a(Block.f);
	}

	@Override
	public TileEntity a(final World world, final int n) {
		return new MovedSoundTileEntityChest();
	}

}
