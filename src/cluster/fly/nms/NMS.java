package cluster.fly.nms;

import org.bukkit.entity.Player;

public interface NMS {

	public void sendActionBar(Player p, String msg);

	public void sendRawJson(Player p, String json);
	
}
