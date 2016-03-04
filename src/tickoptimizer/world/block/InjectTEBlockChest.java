package tickoptimizer.world.block;

import tickoptimizer.world.tileentity.MovedSoundTileEntityChest;
import net.minecraft.server.v1_9_R1.BlockChest;
import net.minecraft.server.v1_9_R1.SoundEffectType;
import net.minecraft.server.v1_9_R1.TileEntity;
import net.minecraft.server.v1_9_R1.World;

public abstract class InjectTEBlockChest extends BlockChest {

	protected InjectTEBlockChest(BlockChest.Type type) {
		super(type);
		c(2.5F);
		a(SoundEffectType.a);
	}

	@Override
	public TileEntity a(final World world, final int n) {
		return new MovedSoundTileEntityChest();
	}

}
