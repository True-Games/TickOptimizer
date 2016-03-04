package tickoptimizer.world.tileentity;

import net.minecraft.server.v1_9_R1.Blocks;
import net.minecraft.server.v1_9_R1.SoundCategory;
import net.minecraft.server.v1_9_R1.SoundEffects;
import net.minecraft.server.v1_9_R1.TileEntityEnderChest;

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
			this.world.a(null, this.position.getX() + 0.5, this.position.getY() + 0.5, this.position.getZ() + 0.5, SoundEffects.X, SoundCategory.BLOCKS, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
		}
	}

	@Override
	public void d() {
		super.d();
		if (this.getWorld() == null) {
			return;
		}
		if (this.g == 0) {
			this.world.a(null, this.position.getX() + 0.5, this.position.getY() + 0.5, this.position.getZ() + 0.5, SoundEffects.V, SoundCategory.BLOCKS, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
		}
	}

}
