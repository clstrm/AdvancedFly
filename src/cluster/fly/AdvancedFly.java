package cluster.fly;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import cluster.fly.nms.NMS;
import cluster.fly.nms.ReflectNMS;
import cluster.fly.time.TimedFlyManager;

public class AdvancedFly extends JavaPlugin {

	public static AdvancedFly f;
	
	private static final String CONFIG_VERSION = "1.1";
	
	public TimedFlyManager timedFly;
	private FileConfiguration data;
	private File datafile;
	
	private NMS nms;

	private String ver;
	public static boolean v1_8;
	
	public FlyManager manager;
	private Restrictions restrictions;
	
	public String noPermission, playerNotFound, exemptionError, enabled, disabled, enabledFor, disabledFor,
		cannotTeleport, overDistance, enabledBy, disabledBy, actionBar, flyExpired;
	boolean exemptionPermission, noTeleportPermission;
	int maxDistance;
	
	@Override
	public void onEnable() {
		f = this;
		
		ver = getVersionAPI();
		if(ver.contains("v1_8") || ver.contains("v1_7")) v1_8 = true;
		
		try {
			nms = new ReflectNMS(ver);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to initialize ReflectNMS for " + ver, e);
		}
		
		
		saveDefaultConfig();
		reloadConfig();
		if(!CONFIG_VERSION.equals(getConfig().getString("configVersion", "1.0"))) {
			updateConfig();
		}
		
		load();
		
		manager = new FlyManager();
		restrictions = new Restrictions();
		restrictions.load();
		
		datafile = new File(getDataFolder() + File.separator + "data.yml");
		if(!datafile.exists()) {
			try {
				datafile.createNewFile();
			} catch(Exception e) {
				getLogger().log(Level.SEVERE, "Failed to create file data.yml", e);
			}
		}
		data = YamlConfiguration.loadConfiguration(datafile);
		
		timedFly = new TimedFlyManager();
		
		Bukkit.getPluginManager().registerEvents(manager, this);
		
		PluginCommand command = getCommand("fly");
		if(command != null) command.setExecutor(this);
		
		new Scheduler();
	}
	
	private void updateConfig() {
		FileConfiguration c = getConfig();
		boolean set = false;
		if(!c.isSet("messages.timerActionBar")) {
			c.set("messages.timerActionBar", "&aFly available: &f{time}");
			set = true;
		}
		if(!c.isSet("messages.flyExpired")) {
			c.set("messages.flyExpired", "&cFly is not more available today.");
			set = true;
		}
		
		if(!c.isSet("tempFly.enable")) {
			c.set("tempFly.enable", false);
			c.set("tempFly.groups.1", 300);
			c.set("tempFly.groups.2", 600);
			c.set("tempFly.groups.3", 900);
			set = true;
		}
		
		
		if(set) {
			c.options().header("Sorry, but we had to update the config and as a result all comments were deleted.\n"
					+ "You can find all information at www.spigotmc.org/resources/advanced-fly.47775/");
			c.options().copyHeader(false);
			saveConfig();
		}
	}

	@Override
	public void onDisable() {
		manager.close();
	}
	
	public void cleanUp() {
		data.set("playerData", null);
		saveData();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("advancedfly.fly") && !sender.hasPermission("advancedfly.admin")) {
			sendMessage(sender, noPermission);
			return true;
		}
		
		if(args.length == 0) {
			toggle(sender);
			return true;
		}
		
		String param = args[0];
		
		if(param.startsWith("-") && sender.hasPermission("advancedfly.admin")) {
			if(param.equalsIgnoreCase("-reload")) {
				reloadConfig();
				load();
				restrictions.load();
				timedFly.reload();
				sendMessage(sender, "§eAdvancedFly §ahas been reloaded.");
				return true;
			}
			if(param.equalsIgnoreCase("-clear")) {
				
			}
			sender.sendMessage("§cUnknown parameter - '" + (param.substring(1)) + "'");
			sender.sendMessage("§creload - reload plugin");
			sender.sendMessage("§cclear - clear daily player data");
			return true;
		}
		
		if(!sender.hasPermission("advancedfly.fly.others")) {
			toggle(sender);
			return true;
		}
		
		Player p = Bukkit.getPlayerExact(param);
		if(p == null) {
			sendMessage(sender, playerNotFound);
			return true;
		}
		
		if(exemptionPermission && p.hasPermission("advancedfly.fly.exempt") && 
				sender instanceof Player) {
			sendMessage(sender, exemptionError);
			return true;
		}
		
		boolean value = !manager.isFlying(p);
		
		if(args.length > 1) {
			if(args[1].equalsIgnoreCase("on")) value = true;
			else if(args[1].equalsIgnoreCase("off")) value = false;
			else {
				sender.sendMessage("§c'" + args[1] + "' is not an on/off parameter.");
				return true;
			}
		}
		
		manager.setFlying(p, sender, value);
		return true;
	}
	
	private void toggle(CommandSender sender) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			manager.setFlying(p, !manager.isFlying(p));
		}
		else sender.sendMessage("§cUsage - /fly <player> [on/off]");
	}
	
	
	public static void sendMessage(CommandSender s, String msg) {
		if(!"none".equalsIgnoreCase(msg)) s.sendMessage(msg);
	}
	
	public FileConfiguration getData() {
		return data;
	}
	
	public void saveData() {
		try {
			data.save(datafile);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private void load() {
		exemptionPermission = getConfig().getBoolean("exemptionPermission");
		noTeleportPermission = getConfig().getBoolean("noTeleportPermission");
		
		maxDistance = getConfig().getInt("maxDistance", 20);
		
		noPermission = getConfig().getString("messages.noPermission", "none").replace("&", "§");
		playerNotFound = getConfig().getString("messages.playerNotFound", "none").replace("&", "§");
		enabled = getConfig().getString("messages.enabled", "none").replace("&", "§");
		disabled = getConfig().getString("messages.disabled", "none").replace("&", "§");
		enabledFor = getConfig().getString("messages.enabledFor", "none").replace("&", "§");
		disabledFor = getConfig().getString("messages.disabledFor", "none").replace("&", "§");
		exemptionError = getConfig().getString("messages.exemptionError", "none").replace("&", "§");
		cannotTeleport = getConfig().getString("messages.cannotTeleport", "none").replace("&", "§");
		overDistance = getConfig().getString("messages.overDistance", "none").replace("&", "§");
		
		enabledBy = getConfig().getString("messages.enabledBy", "none").replace("&", "§");
		disabledBy = getConfig().getString("messages.disabledBy", "none").replace("&", "§");
		flyExpired = getConfig().getString("messages.flyExpired", "none").replace("&", "§");
		actionBar = getConfig().getString("messages.timerActionBar", "{time}").replace("&", "§");
	}
	
	public static String getVersionAPI() {
	    String packageName = f.getServer().getClass().getPackage().getName();
	    return packageName.substring(packageName.lastIndexOf('.') + 1);
	}

	public static NMS nms() {
		return f.nms;
	}
}