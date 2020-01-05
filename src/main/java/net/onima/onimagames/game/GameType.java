package net.onima.onimagames.game;

public enum GameType {
	
	KOTH("KoTH", "§9"),
	CONQUEST("Conquest", "§3"),
	DTC("DTC", "§e"),
	CITADEL("Citadel", "§5§l"),
	DRAGON_EVENT("Dragon", "§1");
	
	private String name, eventColor;
	
	private GameType(String name, String eventColor) {
		this.name = name;
		this.eventColor = eventColor;
	}

	public String getName() {
		return name;
	}
	
	public String getColor() {
		return eventColor;
	}
	
	public static GameType getType(String type) {
		switch (type) {
		case "KOTH":
			return KOTH;
		case "CONQUEST":
			return CONQUEST;
		case "DTC":
			return DTC;
		case "CITADEL":
			return CITADEL;
		case "DRAGON_EVENT":
			return DRAGON_EVENT;
		default:
			return null;
		}
	}
	
}
