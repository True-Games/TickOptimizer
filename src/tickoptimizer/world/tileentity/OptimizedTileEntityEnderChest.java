package tickoptimizer.world.tileentity;

import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.TileEntityEnderChest;

public class OptimizedTileEntityEnderChest extends TileEntityEnderChest {

	private int ticks = 0;

	@Override
	public void c() {
		if (++this.ticks % 20 == 0) {
			this.world.playBlockAction(this.position, Blocks.ENDER_CHEST, 1, this.g);
		}
	}

	@Override
	public void b() {
		super.b();
		if (this.getWorld() == null) {
			return;
		}
		if (this.g == 1) {
			this.world.makeSound(this.position.getX() + 0.5, this.position.getY() + 0.5, this.position.getZ() + 0.5, "random.chestopen", 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
		}
	}

	@Override
	public void d() {
		super.d();
		if (this.getWorld() == null) {
			return;
		}
		if (this.g == 0) {
			this.world.makeSound(this.position.getX() + 0.5, this.position.getY() + 0.5, this.position.getZ() + 0.5, "random.chestclosed", 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
		}
	}

}
