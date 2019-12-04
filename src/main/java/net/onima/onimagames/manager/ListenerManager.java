package net.onima.onimagames.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import net.onima.onimagames.OnimaGames;
import net.onima.onimagames.listener.CapableListener;
import net.onima.onimagames.listener.DTCListener;
import net.onima.onimagames.listener.DragonListener;
import net.onima.onimagames.listener.FactionEventListener;
import net.onima.onimagames.listener.GameMotdListener;

/**
 * This class handles all the bukkit's listeners.
 */
public class ListenerManager {
	
	private OnimaGames plugin;
	
	private PluginManager pm;
	
	public ListenerManager(OnimaGames plugin) {
		this.plugin = plugin;
		this.pm = Bukkit.getPluginManager();
	}
	
	public void registerListener() {
		pm.registerEvents(new CapableListener(), plugin);
		pm.registerEvents(new DTCListener(), plugin);
		pm.registerEvents(new DragonListener(), plugin);
		pm.registerEvents(new FactionEventListener(), plugin);
		pm.registerEvents(new GameMotdListener(), plugin);
	}

}
