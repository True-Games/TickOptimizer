package tickoptimizer.world;

import tickoptimizer.world.tileentity.MovedSoundTileEntityChest;
import tickoptimizer.world.tileentity.OptimizedTileEntityBeacon;
import tickoptimizer.world.tileentity.OptimizedTileEntityEnderChest;
import net.minecraft.server.v1_8_R3.BlockJukeBox.TileEntityRecordPlayer;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntityBanner;
import net.minecraft.server.v1_8_R3.TileEntityBrewingStand;
import net.minecraft.server.v1_8_R3.TileEntityChest;
import net.minecraft.server.v1_8_R3.TileEntityCommand;
import net.minecraft.server.v1_8_R3.TileEntityComparator;
import net.minecraft.server.v1_8_R3.TileEntityDispenser;
import net.minecraft.server.v1_8_R3.TileEntityDropper;
import net.minecraft.server.v1_8_R3.TileEntityEnchantTable;
import net.minecraft.server.v1_8_R3.TileEntityEnderChest;
import net.minecraft.server.v1_8_R3.TileEntityEnderPortal;
import net.minecraft.server.v1_8_R3.TileEntityFlowerPot;
import net.minecraft.server.v1_8_R3.TileEntityFurnace;
import net.minecraft.server.v1_8_R3.TileEntityHopper;
import net.minecraft.server.v1_8_R3.TileEntityLightDetector;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R3.TileEntityNote;
import net.minecraft.server.v1_8_R3.TileEntityPiston;
import net.minecraft.server.v1_8_R3.TileEntitySign;
import net.minecraft.server.v1_8_R3.TileEntitySkull;
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
		canUpdate.put(OptimizedTileEntityBeacon.class, (byte) 1);
		canUpdate.put(OptimizedTileEntityEnderChest.class, (byte) 0);
		canUpdate.put(MovedSoundTileEntityChest.class, (byte) 0);
	}

	public static boolean canUpdate(TileEntity tileentity) {
		Class<?> clazz = tileentity.getClass();
		if (canUpdate.contains(clazz)) {
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
