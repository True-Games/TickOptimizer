package tickoptimizer.world.utils;

import java.util.List;

import net.minecraft.server.v1_8_R2.AxisAlignedBB;
import net.minecraft.server.v1_8_R2.EntityItem;
import net.minecraft.server.v1_8_R2.EntityMinecartHopper;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.IEntitySelector;
import net.minecraft.server.v1_8_R2.IHopper;
import net.minecraft.server.v1_8_R2.IInventory;
import net.minecraft.server.v1_8_R2.IWorldInventory;
import net.minecraft.server.v1_8_R2.InventoryLargeChest;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.TileEntityHopper;
import net.minecraft.server.v1_8_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

public class HopperUtils {

	public static boolean suckItems(IHopper ihopper) {
		IInventory iinventory = HopperInventoryUtils.getInventoryAt(ihopper.getWorld(), ihopper.A(), ihopper.B() + 1.0D, ihopper.C());
		if (iinventory != null) {
			if (HopperInventoryUtils.isInventoryEmpty(iinventory, EnumDirection.DOWN)) {
				return false;
			}

			if (iinventory instanceof IWorldInventory) {
				IWorldInventory worldinventory = (IWorldInventory) iinventory;
				int[] slots = worldinventory.getSlotsForFace(EnumDirection.DOWN);
				for (int i = 0; i < slots.length; ++i) {
					if (suckItemsFrom(ihopper, iinventory, slots[i], EnumDirection.DOWN)) {
						return true;
					}
				}
			} else {
				for (int i = 0; i < iinventory.getSize(); ++i) {
					if (suckItemsFrom(ihopper, iinventory, i, EnumDirection.DOWN)) {
						return true;
					}
				}
			}
		} else {
			for (EntityItem entityitem : getNearbyItems(ihopper.getWorld(), ihopper.A(), ihopper.B() + 1.0D, ihopper.C())) {
				if (suckItem(ihopper, entityitem)) {
					return true;
				}
			}
		}

		return false;
	}

	private static List<EntityItem> getNearbyItems(World world, double x, double y, double z) {
		return world.a(EntityItem.class, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), IEntitySelector.a);
	}

	private static boolean suckItemsFrom(IHopper ihopper, IInventory iinventory, int i, EnumDirection enumdirection) {
		ItemStack itemstack = iinventory.getItem(i);
		if ((itemstack != null) && canSuckItemsFrom(iinventory, itemstack, i, enumdirection)) {
			ItemStack itemstackclone = itemstack.cloneItemStack();
			CraftItemStack oitemstack = CraftItemStack.asCraftMirror(iinventory.splitStack(i, ihopper.getWorld().spigotConfig.hopperAmount));
			InventoryMoveItemEvent event = new InventoryMoveItemEvent(
				iinventory instanceof InventoryLargeChest ? new CraftInventoryDoubleChest((InventoryLargeChest) iinventory) : iinventory.getOwner().getInventory(),
				oitemstack.clone(), ihopper.getOwner().getInventory(), false
			);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				iinventory.setItem(i, itemstackclone);
				if (ihopper instanceof TileEntityHopper) {
					((TileEntityHopper) ihopper).d(ihopper.getWorld().spigotConfig.hopperTransfer);
				} else if (ihopper instanceof EntityMinecartHopper) {
					((EntityMinecartHopper) ihopper).m(ihopper.getWorld().spigotConfig.hopperTransfer / 2);
				}

				return false;
			}

			int origCount = event.getItem().getAmount();
			ItemStack leftover = HopperInventoryUtils.addItemToHopper(ihopper, CraftItemStack.asNMSCopy(event.getItem()));
			if (leftover == null) {
				if (event.getItem().equals(oitemstack)) {
					iinventory.update();
				} else {
					iinventory.setItem(i, itemstackclone);
				}

				return true;
			}

			itemstackclone.count -= origCount - leftover.count;
			iinventory.setItem(i, itemstackclone);
		}

		return false;
	}

	private static boolean canSuckItemsFrom(IInventory iinventory, ItemStack itemstack, int slot, EnumDirection enumdirection) {
		return !(iinventory instanceof IWorldInventory) || ((IWorldInventory) iinventory).canTakeItemThroughFace(slot, itemstack, enumdirection);
	}

	public static boolean suckItem(IHopper hopper, EntityItem entityitem) {
		InventoryPickupItemEvent event = new InventoryPickupItemEvent(hopper.getOwner().getInventory(), (org.bukkit.entity.Item) entityitem.getBukkitEntity());
		entityitem.world.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			ItemStack itemstack = entityitem.getItemStack().cloneItemStack();
			ItemStack leftover = HopperInventoryUtils.addItemToHopper(hopper, itemstack);
			if (leftover != null) {
				entityitem.setItemStack(leftover);
			} else {
				entityitem.die();
				return true;
			}
		}
		return false;
	}

}
