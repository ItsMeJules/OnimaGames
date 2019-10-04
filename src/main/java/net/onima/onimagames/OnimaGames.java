package net.onima.onimagames;

import org.bukkit.plugin.java.JavaPlugin;

import net.onima.onimaapi.OnimaAPI;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimagames.manager.CommandManager;
import net.onima.onimagames.manager.ListenerManager;

public class OnimaGames extends JavaPlugin {
	
	private static OnimaGames instance;
	
	@Override
	public void onEnable() {
		long started = System.currentTimeMillis();
		OnimaAPI.sendConsoleMessage("====================§6[§3ACTIVATION§6]§r====================", ConfigurationService.ONIMAGAMES_PREFIX);
		instance = this;
		registerManager();
		OnimaAPI.sendConsoleMessage("====================§6[§3ACTIVE EN ("+(System.currentTimeMillis()-started)+"ms)§6]§r====================", ConfigurationService.ONIMAGAMES_PREFIX);
	}
	
	public void registerManager() {
		new ListenerManager(this).registerListener();
		new CommandManager(this).registerCommands();
	}

	public void onDisable() {
		OnimaAPI.sendConsoleMessage("====================§6[§cDESACTIVATION§6]§r====================", ConfigurationService.ONIMAGAMES_PREFIX);
	}

	public static OnimaGames getInstance() {
		return instance;
	}
	
}
