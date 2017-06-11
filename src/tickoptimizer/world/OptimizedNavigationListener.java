package tickoptimizer.world;

import java.util.Map.Entry;

import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.NavigationAbstract;
import net.minecraft.server.v1_12_R1.NavigationListener;
import net.minecraft.server.v1_12_R1.PathEntity;
import net.minecraft.server.v1_12_R1.PathPoint;
import net.minecraft.server.v1_12_R1.World;

public class OptimizedNavigationListener extends NavigationListener {

	private final Object2ObjectOpenHashMap<EntityInsentient, NavigationAbstract> navigators = new Object2ObjectOpenHashMap<EntityInsentient, NavigationAbstract>(1000);

	@Override
	public void a(final World world, final BlockPosition blockPosition, final IBlockData blockData, final IBlockData blockData2, final int n) {
		if (!a(world, blockPosition, blockData, blockData2)) {
			return;
		}
		for (Entry<EntityInsentient, NavigationAbstract> entry : navigators.entrySet()) {
			NavigationAbstract navigation = entry.getValue();
			if (!navigation.j()) {
				PathEntity pathentity = navigation.l();
				if (pathentity != null && !pathentity.b()) {
					if (pathentity.d() != 0) {
						PathPoint pathpoint = pathentity.c();
						EntityInsentient insentient = entry.getKey();
						if (
							blockPosition.distanceSquared(
								(pathpoint.a + insentient.locX) / 2.0,
								(pathpoint.b + insentient.locY) / 2.0,
								(pathpoint.c + insentient.locZ) / 2.0
							) < (pathentity.d() - pathentity.e()) * (pathentity.d() - pathentity.e())
						) {
							navigation.k();
						}
					}
				}
			}
		}
	}

	@Override
	public void a(Entity entity) {
		if (entity instanceof EntityInsentient) {
			EntityInsentient insentient = (EntityInsentient) entity;
			NavigationAbstract navigation = insentient.getNavigation();
			if (navigation != null) {
				navigators.put(insentient, navigation);
			}
		}
	}

	@Override
	public void b(Entity entity) {
		navigators.remove(entity);
	}

}
