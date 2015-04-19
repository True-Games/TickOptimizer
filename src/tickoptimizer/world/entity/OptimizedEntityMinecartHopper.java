package tickoptimizer.world.entity;

import java.util.List;

import tickoptimizer.world.utils.HopperUtils;
import net.minecraft.server.v1_8_R2.EntityItem;
import net.minecraft.server.v1_8_R2.EntityMinecartHopper;
import net.minecraft.server.v1_8_R2.IEntitySelector;
import net.minecraft.server.v1_8_R2.World;

public class OptimizedEntityMinecartHopper extends EntityMinecartHopper {

	public OptimizedEntityMinecartHopper(World world) {
		super(world);
	}

	public OptimizedEntityMinecartHopper(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public boolean D() {
		if (HopperUtils.suckItems(this)) {
			return true;
		} else {
			List<EntityItem> items = getWorld().a(EntityItem.class, this.getBoundingBox().grow(0.25D, 0.0D, 0.25D), IEntitySelector.a);
			if (items.size() > 0) {
				HopperUtils.suckItem(this, items.get(0));
			}

			return false;
		}
	}

}
