package tickoptimizer.world.block;

import net.minecraft.server.v1_9_R1.BlockChest;

public class InjectTEBlockTrappedChest extends InjectTEBlockChest {

	public InjectTEBlockTrappedChest() {
		super(BlockChest.Type.TRAP);
		c("chestTrap");
	}

}
