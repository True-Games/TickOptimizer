package tickoptimizer.world.tileentity;

import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.TileEntityChest;

public class MovedSoundTileEntityChest extends TileEntityChest {

	@Override
	public void startOpen(final EntityHuman entityhuman) {
		m();
		super.startOpen(entityhuman);
		if (this.getWorld() == null) {
			return;
		}
		if (this.f == null && this.h == null && this.getViewers().size() == 0) {
			double x = this.position.getX() + 0.5;
			double z = this.position.getZ() + 0.5;
			if (this.i != null) {
				z += 0.5;
			}
			if (this.g != null) {
				x += 0.5;
			}
			this.world.makeSound(x, this.position.getY() + 0.5, z, "random.chestopen", 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
		}
	}

	@Override
	public void closeContainer(final EntityHuman entityhuman) {
		super.closeContainer(entityhuman);
		l = getViewers().size();
		if (this.getWorld() == null) {
			return;
		}
		if (this.f == null && this.h == null && this.getViewers().size() == 1) {
			double x = this.position.getX() + 0.5;
			double z = this.position.getZ() + 0.5;
			if (this.i != null) {
				z += 0.5;
			}
			if (this.g != null) {
				x += 0.5;
			}
			this.world.makeSound(x, this.position.getY() + 0.5, z, "random.chestclosed", 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
		}
	}

}
