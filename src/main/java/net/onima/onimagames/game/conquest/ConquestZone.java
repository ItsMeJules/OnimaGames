package net.onima.onimagames.game.conquest;

import org.bukkit.Bukkit;

import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.zone.Cuboid;
import net.onima.onimaapi.zone.type.utils.Capable;
import net.onima.onimafaction.players.FPlayer;
import net.onima.onimagames.event.capable.CapableCapEvent;
import net.onima.onimagames.event.capable.CapableKnockEvent;

public class ConquestZone implements Capable {
	
	private Conquest conquest;
	private int pointsPerCap;
	private long capTime, capTimeLeft, timeAtCap;
	private APIPlayer capper;
	private String name;
	private ConquestType type;
	private Cuboid capZone;
	
	public ConquestZone(Conquest conquest, ConquestType type, int pointsPerCap, long capTime, APIPlayer capper, String name) {
		this.pointsPerCap = pointsPerCap;
		this.capTime = capTime;
		this.capper = capper;
		this.name = name;
		capTimeLeft = capTime;
		timeAtCap = -1L;
		this.type = type;
	}
	
	public ConquestZone(Conquest conquest, ConquestType type, String name) {
		this(conquest, type, 0, 0, null, name);
	}
	
	public Conquest getConquest() {
		return conquest;
	}
	
	public long getCapTime() {
		return capTime;
	}
	
	public void setCapTime(long capTime) {
		this.capTime = capTime;
		capTimeLeft = capTime;
	}
	
	@Override
	public Cuboid getCapZone() {
		return capZone;
	}
	
	@Override
	public void setCapZone(Cuboid capZone) {
		this.capZone = capZone;
	}

	public long getCapTimeLeft() {
		return capTimeLeft;
	}
	
	public void setCapTimeLeft(long capTimeLeft) {
		this.capTimeLeft = capTimeLeft;
	}
	
	public long getTimeAtCap() {
		return timeAtCap;
	}
	
	public void setTimeAtCap(long timeAtCap) {
		this.timeAtCap = timeAtCap;
	}
	
	public void decreaseTime() {
		capTimeLeft = timeAtCap+capTime-System.currentTimeMillis();
	}
	
	public APIPlayer getCapper() {
		return capper;
	}
	
	public void setCapper(APIPlayer capper) {
		this.capper = capper;
	}

	public boolean isCapped() {
		return capper != null;
	}
	
	public int getPointsPerCap() {
		return pointsPerCap;
	}
	
	public void setPointsPerCap(int pointsPerCap) {
		this.pointsPerCap = pointsPerCap;
	}

	public String getName() {
		return name;
	}
	
	public ConquestType getType() {
		return type;
	}
	
	@Override
	public boolean tryCapping(APIPlayer capper) {
		if (!FPlayer.getPlayer(capper.getUUID()).hasFaction()) {
			capper.sendMessage("§cVous avez besoin d'une faction pour capturer une zone de conquest !");
			return false;
		} else 
			return true;
	}
	
	@Override
	public void onCap(APIPlayer capper) {
		CapableCapEvent event = new CapableCapEvent(this, capper);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return;
		
		capper.sendMessage("§eVous §7êtes entrain de capturer la zone !");
		this.capper = capper;
		capper.setCapping(this);
		timeAtCap = System.currentTimeMillis();
	}
	
	@Override
	public void onKnock(APIPlayer capper, APIPlayer knocker) {
		CapableKnockEvent event = new CapableKnockEvent(this, knocker, capper);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return;
		
		capper.sendMessage("§eVous §7n'êtes plus entrain de capturer la zone !");
		capper.setCapping(this);
		this.capper = null;
		timeAtCap = -1L;
		capTimeLeft = capTime;
	}
	
}
