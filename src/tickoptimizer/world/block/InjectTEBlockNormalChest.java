package tickoptimizer.world.block;

import net.minecraft.server.v1_9_R1.BlockChest;

public class InjectTEBlockNormalChest extends InjectTEBlockChest {

	public InjectTEBlockNormalChest() {
		super(BlockChest.Type.BASIC);
		c("chest");
	}

}
