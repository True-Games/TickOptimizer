package tickoptimizer.world;

import net.minecraft.server.v1_8_R2.BlockJukeBox.TileEntityRecordPlayer;
import net.minecraft.server.v1_8_R2.TileEntity;
import net.minecraft.server.v1_8_R2.TileEntityBanner;
import net.minecraft.server.v1_8_R2.TileEntityBrewingStand;
import net.minecraft.server.v1_8_R2.TileEntityChest;
import net.minecraft.server.v1_8_R2.TileEntityCommand;
import net.minecraft.server.v1_8_R2.TileEntityComparator;
import net.minecraft.server.v1_8_R2.TileEntityDispenser;
import net.minecraft.server.v1_8_R2.TileEntityDropper;
import net.minecraft.server.v1_8_R2.TileEntityEnchantTable;
import net.minecraft.server.v1_8_R2.TileEntityEnderChest;
import net.minecraft.server.v1_8_R2.TileEntityEnderPortal;
import net.minecraft.server.v1_8_R2.TileEntityFlowerPot;
import net.minecraft.server.v1_8_R2.TileEntityFurnace;
import net.minecraft.server.v1_8_R2.TileEntityHopper;
import net.minecraft.server.v1_8_R2.TileEntityLightDetector;
import net.minecraft.server.v1_8_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R2.TileEntityNote;
import net.minecraft.server.v1_8_R2.TileEntityPiston;
import net.minecraft.server.v1_8_R2.TileEntitySign;
import net.minecraft.server.v1_8_R2.TileEntitySkull;
import gnu.trove.map.hash.TObjectByteHashMap;

public class TileEntityCanUpdateCheck {

	private static TObjectByteHashMap<Class<? extends TileEntity>> canUpdate = new TObjectByteHashMap<Class<? extends TileEntity>>();

	static {
		canUpdate.put(TileEntityBanner.class, (byte) 0);
		canUpdate.put(TileEntityBrewingStand.class, (byte) 1);
		canUpdate.put(TileEntityChest.class, (byte) 0);
		canUpdate.put(TileEntityCommand.class, (byte) 0);
		canUpdate.put(TileEntityComparator.class, (byte) 0);
		canUpdate.put(TileEntityDispenser.class, (byte) 0);
		canUpdate.put(TileEntityDropper.class, (byte) 0);
		canUpdate.put(TileEntityEnchantTable.class, (byte) 0);
		canUpdate.put(TileEntityEnderChest.class, (byte) 1);
		canUpdate.put(TileEntityEnderPortal.class, (byte) 0);
		canUpdate.put(TileEntityFlowerPot.class, (byte) 0);
		canUpdate.put(TileEntityFurnace.class, (byte) 1);
		canUpdate.put(TileEntityHopper.class, (byte) 1);
		canUpdate.put(TileEntityLightDetector.class, (byte) 1);
		canUpdate.put(TileEntityMobSpawner.class, (byte) 1);
		canUpdate.put(TileEntityNote.class, (byte) 0);
		canUpdate.put(TileEntityPiston.class, (byte) 1);
		canUpdate.put(TileEntityRecordPlayer.class, (byte) 0);
		canUpdate.put(TileEntitySign.class, (byte) 0);
		canUpdate.put(TileEntitySkull.class, (byte) 0);
	}

	public static boolean canUpdate(TileEntity tileentity) {
		Class<?> clazz = tileentity.getClass();
		if (canUpdate.containsKey(clazz)) {
			return canUpdate.get(clazz) == 0 ? false : true;
		}
		return checkAndRememberCanUpdate(tileentity);
	}

	private static boolean checkAndRememberCanUpdate(TileEntity tileentity) {
		if (
			tileentity instanceof TileEntityBanner ||
			tileentity instanceof TileEntityChest ||
			tileentity instanceof TileEntityCommand ||
			tileentity instanceof TileEntityComparator ||
			tileentity instanceof TileEntityDispenser ||
			tileentity instanceof TileEntityEnchantTable ||
			tileentity instanceof TileEntityEnderPortal ||
			tileentity instanceof TileEntityFlowerPot ||
			tileentity instanceof TileEntityNote ||
			tileentity instanceof TileEntityRecordPlayer ||
			tileentity instanceof TileEntitySign ||
			tileentity instanceof TileEntitySkull
		) {
			canUpdate.put(tileentity.getClass(), (byte) 0);
			return false;
		}
		canUpdate.put(tileentity.getClass(), (byte) 1);
		return true;
	}

}
