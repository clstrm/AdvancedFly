package cluster.fly.nms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

public class ReflectNMS implements NMS {
;
	private Class<?> iChatBaseComponent, packet, craftPlayer, entityPlayer, connection;
	private Method a, getHandle, sendPacket;
	private Field playerConnection;
	
	private Class<?> chatMessageType;
	private Object enumChatMessageType;
	
	public ReflectNMS(String ver) throws Exception {
		iChatBaseComponent = Class.forName("net.minecraft.server." + ver + ".IChatBaseComponent");
		Class<?> p = Class.forName("net.minecraft.server." + ver + ".Packet");
		packet = Class.forName("net.minecraft.server." + ver + ".PacketPlayOutChat");
		connection = Class.forName("net.minecraft.server." + ver + ".PlayerConnection");
		entityPlayer = Class.forName("net.minecraft.server." + ver + ".EntityPlayer");
		craftPlayer = Class.forName("org.bukkit.craftbukkit." + ver + ".entity.CraftPlayer");
		getHandle = craftPlayer.getMethod("getHandle");
		playerConnection = entityPlayer.getField("playerConnection");
		sendPacket = connection.getMethod("sendPacket", p);
		
		Class<?> serializer;
		try {
			serializer =
					Class.forName("net.minecraft.server." + ver + ".IChatBaseComponent$ChatSerializer");
		} catch (ClassNotFoundException e) {
			serializer =
					Class.forName("net.minecraft.server." + ver + ".ChatSerializer");
		}
		
		a = serializer.getMethod("a", String.class);
		
		
		try {
			chatMessageType = Class.forName("net.minecraft.server." + ver + ".ChatMessageType");
			Method valueOf = chatMessageType.getMethod("valueOf", String.class);
			enumChatMessageType = valueOf.invoke(null, "GAME_INFO");
		} catch(Exception e) {
		}
	}
	
	@Override
	public void sendActionBar(Player p, String msg) {
		try {
			Object iChatBaseComponent = a.invoke(null, "{\"text\":\"" + msg + "\"}");
			Object packetPlayOutChat = this.chatMessageType == null ?
						this.packet.getConstructor(this.iChatBaseComponent, byte.class)
						.newInstance(iChatBaseComponent, (byte) 2)
					:
						this.packet.getConstructor(this.iChatBaseComponent, this.chatMessageType)
						.newInstance(iChatBaseComponent, enumChatMessageType);
			Object entityPlayer = getHandle.invoke(p);
			Object playerConnection = this.playerConnection.get(entityPlayer);
			sendPacket.invoke(playerConnection, packetPlayOutChat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendRawJson(Player p, String json) {
		try {
			Object iChatBaseComponent = a.invoke(null, json);
			Object packetPlayOutChat = 
					this.packet.getConstructor(this.iChatBaseComponent).newInstance(iChatBaseComponent);
			Object entityPlayer = getHandle.invoke(p);
			Object playerConnection = this.playerConnection.get(entityPlayer);
			sendPacket.invoke(playerConnection, packetPlayOutChat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
