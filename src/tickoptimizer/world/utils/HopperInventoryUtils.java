package tickoptimizer.world.utils;

import java.util.List;

import net.minecraft.server.v1_8_R2.AxisAlignedBB;
import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockChest;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.IEntitySelector;
import net.minecraft.server.v1_8_R2.IHopper;
import net.minecraft.server.v1_8_R2.IInventory;
import net.minecraft.server.v1_8_R2.IWorldInventory;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.MathHelper;
import net.minecraft.server.v1_8_R2.TileEntity;
import net.minecraft.server.v1_8_R2.TileEntityChest;
import net.minecraft.server.v1_8_R2.TileEntityHopper;
import net.minecraft.server.v1_8_R2.World;

public class HopperInventoryUtils {

	public static ItemStack addItemToHopper(IHopper ihopper, ItemStack stack) {
		boolean added = false;
		ItemStack[] containeritems = ihopper.getContents();
		for (int i = 0; i < containeritems.length; i++) {
			ItemStack containeritem = containeritems[i];
			if (containeritem == null) {
				containeritems[i] = stack;
				stack = null;
				added = true;
				break;
			} else if (containeritem.count < containeritem.getMaxStackSize() && ItemStack.equals(containeritem, stack)) {
				int addcount = Math.min(stack.count, containeritem.getMaxStackSize() - containeritem.count);
				containeritem.count += addcount;
				stack.count -= addcount;
				added = addcount > 0;
				if (stack.count == 0) {
					stack = null;
					break;
				}
			}
		}

		if (added && ihopper instanceof TileEntityHopper) {
			TileEntityHopper tileentityhopper1 = (TileEntityHopper) ihopper;
			if (tileentityhopper1.o()) {
				tileentityhopper1.d(tileentityhopper1.getWorld().spigotConfig.hopperTransfer);
			}
			ihopper.update();
		}

		return stack;
	}

	public static IInventory getInventoryAt(World world, double x, double y, double z) {
		BlockPosition blockposition = new BlockPosition(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
		if (!world.isLoaded(blockposition)) {
			return null;
		} else {
			IInventory inventory = null;
			Block block = world.getType(blockposition).getBlock();
			if (block.isTileEntity()) {
				TileEntity tileentity = world.getTileEntity(blockposition);
				if (tileentity instanceof IInventory) {
					inventory = (IInventory) tileentity;
					if ((inventory instanceof TileEntityChest) && (block instanceof BlockChest)) {
						inventory = ((BlockChest) block).f(world, blockposition);
					}
				}
			}

			if (inventory == null) {
				List<?> invetoryentitylist = world.a((Entity) null, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), IEntitySelector.c);
				if (invetoryentitylist.size() > 0) {
					inventory = (IInventory) invetoryentitylist.get(world.random.nextInt(invetoryentitylist.size()));
				}
			}

			return inventory;
		}
	}

	public static boolean isInventoryEmpty(IInventory iinventory, EnumDirection enumdirection) {
		if (iinventory instanceof IWorldInventory) {
			IWorldInventory worldinventory = (IWorldInventory) iinventory;
			for (int slot : worldinventory.getSlotsForFace(enumdirection)) {
				if (worldinventory.getItem(slot) != null) {
					return false;
				}
			}
		} else {
			for (int i = 0; i < iinventory.getSize(); i++) {
				if (iinventory.getItem(i) != null) {
					return false;
				}
			}
		}

		return true;
	}

}
