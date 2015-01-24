package tickoptimizer.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import tickoptimizer.world.WorldInjector;

public class WorldListener implements Listener {

	@EventHandler(priority=EventPriority.LOWEST)
	public void onWorldInit(WorldInitEvent event) {
		WorldInjector.inject(event.getWorld());
	}

}
