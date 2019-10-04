package net.onima.onimagames.manager;

import net.onima.onimagames.OnimaGames;
import net.onima.onimagames.commands.citadel.CitadelExecutor;
import net.onima.onimagames.commands.conquest.ConquestExecutor;
import net.onima.onimagames.commands.dragon.DragonExecutor;
import net.onima.onimagames.commands.dtc.DTCExecutor;
import net.onima.onimagames.commands.game.GameExecutor;
import net.onima.onimagames.commands.koth.KothExecutor;

public class CommandManager {

	private OnimaGames plugin;
	
	public CommandManager(OnimaGames plugin) {
		this.plugin = plugin;
	}
	
	public void registerCommands() {
		plugin.getCommand("game").setExecutor(new GameExecutor());
		plugin.getCommand("koth").setExecutor(new KothExecutor());
		plugin.getCommand("conquest").setExecutor(new ConquestExecutor());
		plugin.getCommand("citadel").setExecutor(new CitadelExecutor());
		plugin.getCommand("dtc").setExecutor(new DTCExecutor());
		plugin.getCommand("dragon").setExecutor(new DragonExecutor());
	}

}
