package net.onima.onimagames.commands.game.arguments;

import org.apache.commons.lang.WordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.commands.game.GameExecutor;

public class GameHelpArgument extends BasicCommandArgument {
	
	private GameExecutor executor;

	public GameHelpArgument(GameExecutor executor) {
		super("help", OnimaPerm.GAME_HELP_ARGUMENT);
		usage = new JSONMessage("§7/game " + name + " (page)", "§d§oAffiche l'aide pour les events.");
		this.executor = executor;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) {
			showPage(sender, label, 1);
			return true;
		}
		
		Integer page = Methods.toInteger(args[1]);
		
		if (page != null) showPage(sender, label, page);
		else showPage(sender, label, 1);
			
		return true;
	}
	
	private void showPage(CommandSender sender, String label, int pageNumber) {
		Multimap<Integer, JSONMessage> pages = ArrayListMultimap.create();
		int helps = 0;
		int index = 1;
		
		for (BasicCommandArgument bca : executor.getArguments()) {
				
			if (bca.equals(this)) continue;
			
			OnimaPerm permission = bca.getPermission();
				
			if (permission != null && !sender.hasPermission(permission.getPermission())) continue;
			if (bca.isPlayerOnly() && !(sender instanceof Player)) continue;
				
			helps++;
				
			pages.get(index).add(bca.getUsage());
			
			if (helps == ConfigurationService.HELP_PER_PAGE) {
				index++;
				helps = 0;
			}
		}
		
		if (pages.isEmpty()) {
			sender.sendMessage("§d§oAucune §7aide disponible, §d§ovous §7n'avez sûrement pas accès aux §d§ocommandes§7.");
			return;
		}
		
		if (!pages.containsKey(pageNumber)) {
			sender.sendMessage("§cLa page " + pageNumber + " n'existe pas.");
			return;
		}
		
		sender.sendMessage("§e" + ConfigurationService.STAIGHT_LINE);
		if (pages.keySet().size() > pageNumber) {
			Methods.sendJSON(sender, new ComponentBuilder("§d§o" + WordUtils.capitalizeFully(label + "§e" + " - " + "§7" + "(Page " + pageNumber + '/' + pages.keySet().size() + ')'))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game help " + (pageNumber + 1)))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§6Cliquez pour afficher la page " + (pageNumber + 1)).create())).create());
		} else
			sender.sendMessage("§d§o" + WordUtils.capitalizeFully(label + "§e" + " - " + "§7" + "(Page " + pageNumber + '/' + pages.keySet().size() + ')'));
		
		for(JSONMessage message : pages.get(pageNumber)) 
			message.send(sender);
		sender.sendMessage("§e" + ConfigurationService.STAIGHT_LINE);

	}

}
