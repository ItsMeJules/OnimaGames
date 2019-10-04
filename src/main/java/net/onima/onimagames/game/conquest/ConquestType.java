package net.onima.onimagames.game.conquest;

import java.util.ArrayList;
import java.util.List;

public enum ConquestType {
	
	RED("§crouge"),
	GREEN("§avert"),
	YELLOW("§ejaune"),
	BLUE("§9bleu"),
	MAIN("§dprincipale");
	
	private String name;

	private ConquestType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public static List<String> toStringList() {
		List<String> list = new ArrayList<>();
		
		for (ConquestType type : values())
			list.add(type.name.toUpperCase());
	
		return list;
	}
	
	public static ConquestType fromString(String str) {
		switch (str) {
		case "RED":
			return RED;
		case "GREEN":
			return GREEN;
		case "YELLOW":
			return YELLOW;
		case "BLUE":
			return BLUE;
		case "MAIN":
			return MAIN;
		default:
			return null;
		}
	}

}
