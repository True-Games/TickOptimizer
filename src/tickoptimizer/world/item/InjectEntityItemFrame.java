package tickoptimizer.world.item;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;

import tickoptimizer.world.entity.OptimizedEntityItemFrame;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EnumDirection;
import net.minecraft.server.v1_8_R1.ItemHanging;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.craftbukkit.v1_8_R1.block.CraftBlock;

public class InjectEntityItemFrame extends ItemHanging {

	public InjectEntityItemFrame() {
		super(OptimizedEntityItemFrame.class);
		c("frame");
	}

	@Override
	public boolean interactWith(final ItemStack itemstack, final EntityHuman entityhuman, final World world, final BlockPosition blockposition, final EnumDirection enumdirection, final float f, final float f1, final float f2) {
		if (enumdirection == EnumDirection.DOWN || enumdirection == EnumDirection.UP) {
			return false;
		}
		final BlockPosition shiftedpos = blockposition.shift(enumdirection);
		if (!entityhuman.a(shiftedpos, enumdirection, itemstack)) {
			return false;
		}
		final OptimizedEntityItemFrame frame = new OptimizedEntityItemFrame(world, shiftedpos, enumdirection);
		if (frame.canPlaceAt()) {
			final Player who = ((entityhuman == null) ? null : ((Player) entityhuman.getBukkitEntity()));
			final Block blockClicked = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
			final BlockFace blockFace = CraftBlock.notchToBlockFace(enumdirection);
			final HangingPlaceEvent event = new HangingPlaceEvent((Hanging) frame.getBukkitEntity(), who, blockClicked, blockFace);
			world.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return false;
			}
			world.addEntity(frame);
			--itemstack.count;
		}
		return true;
	}

}
