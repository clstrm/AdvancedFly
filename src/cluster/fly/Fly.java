package cluster.fly;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Fly {

	private Player player;
	private Location loc;
	private boolean locked;

	public Fly(Player player) {
		this.player = player;
		this.loc = player.getLocation();
	}
	
	public Fly(Player player, Location loc) {
		this.player = player;
		this.loc = loc;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public void teleport() {
		lock();
		try {
			player.teleport(loc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		unlock();
	}

	private void lock() {
		locked = true;
	}

	private void unlock() {
		locked = false;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	
}
