package net.onima.onimagames.commands.game.arguments.staff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimafaction.faction.Faction;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.citadel.Citadel;
import net.onima.onimagames.game.conquest.Conquest;
import net.onima.onimagames.game.dragon.Dragon;
import net.onima.onimagames.game.dtc.DTC;
import net.onima.onimagames.game.koth.Koth;

public class GameCreateArgument extends BasicCommandArgument {

	public GameCreateArgument() {
		super("create", OnimaPerm.GAME_CREATE_ARGUMENT);
		usage = new JSONMessage("§7/game " + name + " <name> <type>", "§d§oCréée un nouvel event.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 3) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		if (Faction.getFaction(args[1]) != null) {
			sender.sendMessage("§cUne faction s'appelle déjà " + args[1] + ", changez de nom.");
			return false;
		}
		
		GameType type = GameType.getType(args[2]);
		
		if (type == null) {
			sender.sendMessage("§cAucun mode de jeu ne s'appelle : " + args[2]);
			return false;
		}
		
		switch (type) {
		case CITADEL:
			new Citadel(args[1], sender.getName());
			break;
		case CONQUEST:
			new Conquest(args[1], sender.getName());
			break;
		case DRAGON_EVENT:
			new Dragon(args[1], sender.getName());
			break;
		case DTC:
			new DTC(args[1], sender.getName());
			break;
		case KOTH:
			new Koth(args[1], sender.getName());
			break;
		default:
			return false; //Never happens
		}
		
		sender.sendMessage("§d§oVous §7avez créé l'event §d§o" + type.getName() + ' ' + args[1] + "§7.");
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		
		if (args.length == 3) {
			for (GameType type : GameType.values()) {
				if (StringUtil.startsWithIgnoreCase(type.name(), args[2]))
					completions.add(type.name());
			}
		}
		
		return completions;
	}

}
