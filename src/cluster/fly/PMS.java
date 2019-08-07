package cluster.fly;

import org.bukkit.entity.Player;

public enum PMS {
			  
	damage, pvp, potion, bow, projectile, block, hand;
	
	public boolean has(Player p) {
		return p.hasPermission("advancedfly.bypass." + name());
	}
}
