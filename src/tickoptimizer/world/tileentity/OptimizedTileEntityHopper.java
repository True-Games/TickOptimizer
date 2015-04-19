package tickoptimizer.world.tileentity;

import java.util.Arrays;

import net.minecraft.server.v1_8_R2.BlockHopper;
import net.minecraft.server.v1_8_R2.EnumDirection;
import net.minecraft.server.v1_8_R2.IInventory;
import net.minecraft.server.v1_8_R2.IWorldInventory;
import net.minecraft.server.v1_8_R2.InventoryLargeChest;
import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagList;
import net.minecraft.server.v1_8_R2.TileEntityHopper;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;

import tickoptimizer.world.utils.HopperInventoryUtils;
import tickoptimizer.world.utils.HopperUtils;

public class OptimizedTileEntityHopper extends TileEntityHopper {

	private String customname;
	private int transfercooldown = -1;

	@Override
	public void a(NBTTagCompound nbttagcompound) {
		super.a(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getList("Items", 10);
		ItemStack[] items = getContents();
		Arrays.fill(items, null);
		if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
			customname = nbttagcompound.getString("CustomName");
		}

		transfercooldown = nbttagcompound.getInt("TransferCooldown");

		for (int i = 0; i < nbttaglist.size(); ++i) {
			NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
			byte slot = nbttagcompound1.getByte("Slot");
			if ((slot >= 0) && (slot < items.length)) {
				items[slot] = ItemStack.createStack(nbttagcompound1);
			}
		}

	}

	@Override
	public void b(NBTTagCompound nbttagcompound) {
		super.b(nbttagcompound);
		NBTTagList nbttaglist = new NBTTagList();

		ItemStack[] items = getContents();
		for (int slot = 0; slot < getSize(); ++slot) {
			if (items[slot] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) slot);
				items[slot].save(nbttagcompound1);
				nbttaglist.add(nbttagcompound1);
			}
		}

		nbttagcompound.set("Items", nbttaglist);
		nbttagcompound.setInt("TransferCooldown", transfercooldown);
		if (hasCustomName()) {
			nbttagcompound.setString("CustomName", customname);
		}
	}

	@Override
	public String getName() {
		return hasCustomName() ? customname : "container.hopper";
	}

	@Override
	public boolean hasCustomName() {
		return (customname != null) && (customname.length() > 0);
	}

	@Override
	public void a(String s) {
		customname = s;
	}

	@Override
	public void c() {
		if (world != null) {
			--transfercooldown;
			if (!n()) {
				transfercooldown = 0;
				m();
			}
		}
	}

	@Override
	public boolean m() {
		if (world != null) {
			if (!n() && BlockHopper.f(u())) {
				boolean transferred = false;
				if (!isHopperEmpty()) {
					transferred = transferItem();
				}

				if (!isHopperFull()) {
					transferred |= HopperUtils.suckItems(this);
				}

				if (transferred) {
					transfercooldown = world.spigotConfig.hopperTransfer;
					update();
					return true;
				}
			}

			return false;
		} else {
			return false;
		}
	}

	private boolean isHopperEmpty() {
		for (ItemStack itemstack : getContents()) {
			if (itemstack != null) {
				return false;
			}
		}
		return true;
	}

	private boolean isHopperFull() {
		for (ItemStack itemstack : getContents()) {
			if ((itemstack == null) || (itemstack.count != itemstack.getMaxStackSize())) {
				return false;
			}
		}
		return true;
	}

	private boolean transferItem() {
		IInventory iinventory = getConnectedOutputInventory();
		if (iinventory == null) {
			return false;
		}
		EnumDirection outputSide = BlockHopper.b(u()).opposite();
		if (this.isInventoryFull(iinventory, outputSide)) {
			return false;
		}
		for (int i = 0; i < getSize(); ++i) {
			if (getItem(i) != null) {
				ItemStack itemstack = getItem(i).cloneItemStack();
				CraftItemStack oitemstack = CraftItemStack.asCraftMirror(splitStack(i, world.spigotConfig.hopperAmount));
				Inventory destinationInventory;
				if (iinventory instanceof InventoryLargeChest) {
					destinationInventory = new CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
				} else {
					destinationInventory = iinventory.getOwner().getInventory();
				}

				InventoryMoveItemEvent event = new InventoryMoveItemEvent(getOwner().getInventory(), oitemstack.clone(), destinationInventory, true);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					setItem(i, itemstack);
					transfercooldown = world.spigotConfig.hopperTransfer;
					return false;
				}

				int origCount = event.getItem().getAmount();
				ItemStack leftover = addItem(iinventory, CraftItemStack.asNMSCopy(event.getItem()), outputSide);
				if (leftover == null) {
					if (event.getItem().equals(oitemstack)) {
						iinventory.update();
					} else {
						setItem(i, itemstack);
					}

					return true;
				}

				itemstack.count -= origCount - leftover.count;
				setItem(i, itemstack);
			}
		}
		return false;
	}


	private boolean isInventoryFull(IInventory iinventory, EnumDirection enumdirection) {
		if (iinventory instanceof IWorldInventory) {
			IWorldInventory worldinventory = (IWorldInventory) iinventory;
			int[] slots = worldinventory.getSlotsForFace(enumdirection);
			for (int i = 0; i < slots.length; ++i) {
				ItemStack itemstack = worldinventory.getItem(slots[i]);
				if ((itemstack == null) || (itemstack.count != itemstack.getMaxStackSize())) {
					return false;
				}
			}
		} else {
			for (int i = 0; i < iinventory.getSize(); i++) {
				ItemStack itemstack = iinventory.getItem(i);
				if ((itemstack == null) || (itemstack.count != itemstack.getMaxStackSize())) {
					return false;
				}
			}
		}

		return true;
	}

	private IInventory getConnectedOutputInventory() {
		EnumDirection enumdirection = BlockHopper.b(u());
		return HopperInventoryUtils.getInventoryAt(
			getWorld(),
			position.getX() + enumdirection.getAdjacentX(),
			position.getY() + enumdirection.getAdjacentY(),
			position.getZ() + enumdirection.getAdjacentZ()
		);
	}

	@Override
	public void d(int i) {
		transfercooldown = i;
	}

	@Override
	public boolean n() {
		return transfercooldown > 0;
	}

	@Override
	public boolean o() {
		return transfercooldown <= 1;
	}

}
