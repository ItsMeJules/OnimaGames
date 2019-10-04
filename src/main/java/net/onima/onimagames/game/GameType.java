package net.onima.onimagames.game;

public enum GameType {
	
	KOTH("KoTH"),
	CONQUEST("Conquest"),
	DTC("DTC"),
	CITADEL("Citadel"),
	DRAGON_EVENT("Dragon");
	
	private String name;
	
	private GameType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
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
