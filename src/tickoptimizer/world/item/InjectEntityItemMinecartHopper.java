package tickoptimizer.world.item;

import org.bukkit.craftbukkit.v1_8_R2.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import tickoptimizer.world.entity.OptimizedEntityMinecartHopper;
import net.minecraft.server.v1_8_R2.BlockMinecartTrackAbstract;
import net.minecraft.server.v1_8_R2.BlockMinecartTrackAbstract.EnumTrackPosition;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.EntityMinecartAbstract.EnumMinecartType;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.ItemMinecart;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.World;

public class InjectEntityItemMinecartHopper extends ItemMinecart {

	public InjectEntityItemMinecartHopper() {
		super(EnumMinecartType.HOPPER);
		c("minecartHopper");
	}

	@Override
	public boolean interactWith(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2) {
		IBlockData iblockdata = world.getType(blockposition);
		if (BlockMinecartTrackAbstract.d(iblockdata)) {
			EnumTrackPosition trackposition = iblockdata.getBlock() instanceof BlockMinecartTrackAbstract ? (EnumTrackPosition) iblockdata.get(((BlockMinecartTrackAbstract) iblockdata.getBlock()).n()) : EnumTrackPosition.NORTH_SOUTH;
			double yadd = 0.0D;
			if (trackposition.c()) {
				yadd = 0.5D;
			}

			PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(entityhuman, Action.RIGHT_CLICK_BLOCK, blockposition, enumdirection, itemstack);
			if (event.isCancelled()) {
				return false;
			}

			OptimizedEntityMinecartHopper entityminecarthopper = new OptimizedEntityMinecartHopper(world, blockposition.getX() + 0.5D, blockposition.getY() + 0.0625D + yadd, blockposition.getZ() + 0.5D);
			if (itemstack.hasName()) {
				entityminecarthopper.setCustomName(itemstack.getName());
			}

			world.addEntity(entityminecarthopper);

			--itemstack.count;
			return true;
		} else {
			return false;
		}
	}

}
