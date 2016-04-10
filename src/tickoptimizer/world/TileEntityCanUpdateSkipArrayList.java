package tickoptimizer.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.minecraft.server.v1_9_R1.TileEntity;

@SuppressWarnings("serial")
public class TileEntityCanUpdateSkipArrayList extends ArrayList<TileEntity> {

	@Override
	public boolean add(TileEntity tileentity) {
		if (TileEntityCanUpdateCheck.canUpdate(tileentity)) {
			super.add(tileentity);
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends TileEntity> collection) {
		Iterator<? extends TileEntity> iterator = collection.iterator();
		while (iterator.hasNext()) {
			TileEntity tileentity = iterator.next();
			if (!TileEntityCanUpdateCheck.canUpdate(tileentity)) {
				iterator.remove();
			}
		}
		return super.addAll(collection);
	}

}
