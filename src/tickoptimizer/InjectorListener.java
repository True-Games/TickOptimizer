package tickoptimizer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import tickoptimizer.world.WorldInjector;

public class InjectorListener implements Listener {

	@EventHandler(priority=EventPriority.LOWEST)
	public void onWorldInit(WorldInitEvent event) {
		WorldInjector.inject(event.getWorld());
	}

}
