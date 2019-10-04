package net.onima.onimagames.commands.dragon.arguments.staff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.dragon.Dragon;

public class DragonHealthArgument extends BasicCommandArgument {

	public DragonHealthArgument() {
		super("health", OnimaPerm.DRAGON_HEALTH_ARGUMENT);
		usage = new JSONMessage("§7/dragon " + name + " <set | check | remove> <dragon> (health)", "§d§oDéfini la vie du dragon avant ou après avoir été spawn.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 3) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		Game game = null;
		
		if ((game = Game.getGame(args[1])) == null) {
			sender.sendMessage("§cL'event " + args[1] + " n'existe pas !");
			return false;
		}
		
		if (!(game instanceof Dragon)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas un " + GameType.DRAGON_EVENT.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		Dragon dragon = (Dragon) game;

		if (args.length > 3) {
			Double health = Methods.toDouble(args[3]);
			
			if (health == null) {
				sender.sendMessage("§cLa valeur " + args[3] + " n'est pas un nombre.");
				return false;
			}
			
			if (health > 200.0D) {
				sender.sendMessage("§cLes mobs peuvent avoir un maximum de 200.0 points de vie, me demandez pas pourquoi j'en ai aucune putain d'idée.");
				return false;
			}
			
			if (args[1].equalsIgnoreCase("set")) {
				if (dragon.getEntity() != null) {
					EnderDragon entity = dragon.getEntity();
					entity.setMaxHealth(health);
					entity.setHealth(health);
				}
				
				dragon.setDragonHealth(health);
				sender.sendMessage("§d§oVous §7avez défini le nombre de points de l'event §d§o" + GameType.DRAGON_EVENT.getName() + ' ' + dragon.getName() + " §7sur : §d§o" + health + "§7.");
				return true;
			} else if (args[1].equalsIgnoreCase("remove")) {
				if (dragon.getEntity() != null) {
					EnderDragon entity = dragon.getEntity();
					entity.setHealth(dragon.getEntityHealth() - health);
				}
				
				dragon.setDragonHealth(dragon.getDragonHealth() - health);
				sender.sendMessage("§d§oVous §7avez enlevé §d§o" + health + " §7points de vie à l'event §d§o" + GameType.DRAGON_EVENT.getName() + ' ' + dragon.getName() + "§7.");
				return true;
			} else if (args[1].equalsIgnoreCase("check"))
				return checkArg(dragon, sender);
			else
				usage.send(sender, "§7Utilisation : ");
			
		} else if (args[1].equalsIgnoreCase("check"))
			return checkArg(dragon, sender);
		else
			usage.send(sender, "§7Utilisation : ");
		
		return false;
	}
	
	private boolean  checkArg(Dragon dragon, CommandSender sender) {
		sender.sendMessage("§7" + ConfigurationService.STAIGHT_LINE);
		sender.sendMessage("§7Si le dragon n'a pas été spawn, il commencera avec §d§o" + dragon.getDragonHealth() + " §7points de vie.");
		if (dragon.getEntity() != null) {
			sender.sendMessage("§7Le §d§odragon §7a un maximum de §d§o" + dragon.getEntityMaxHealth() + " §7points de vie.");
			sender.sendMessage("§7Le §d§odragon §7a actuellement §d§o" + dragon.getEntityHealth() + " §7points de vie.");
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 3)
			return Game.getGames().parallelStream().filter(game -> game instanceof Dragon).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
		else if (args.length == 2) {
			List<String> completions = new ArrayList<>();
			
			if (StringUtil.startsWithIgnoreCase("set", args[1]))
				completions.add("set");
			
			if (StringUtil.startsWithIgnoreCase("check", args[1]))
				completions.add("check");
			
			if (StringUtil.startsWithIgnoreCase("remove", args[1]))
				completions.add("remove");
			
			return completions;
		}
		
		return Collections.emptyList();
	}

}
