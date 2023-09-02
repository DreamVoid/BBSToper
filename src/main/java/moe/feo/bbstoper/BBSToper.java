package moe.feo.bbstoper;

import me.dreamvoid.bbstoper.command.BBSToperCommand;
import moe.feo.bbstoper.config.Message;
import moe.feo.bbstoper.config.Config;
import moe.feo.bbstoper.listener.Reminder;
import moe.feo.bbstoper.utils.Util;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import moe.feo.bbstoper.listener.InventoryListener;
import moe.feo.bbstoper.database.DatabaseManager;

import java.io.File;

public class BBSToper extends JavaPlugin {
	public static BBSToper INSTANCE;

	@Override
	public void onLoad() {
		INSTANCE = this;
		getLogger().info("Loading configuration.");
		saveDefaultConfig();
		Config.load();
		saveResource("lang.yml", false);
		Message.load();
	}

	@Override
	public void onEnable() {
		getLogger().info("Initializing database.");
		DatabaseManager.initializeDatabase();

		getLogger().info("Registering commands.");
		getCommand("bbstoper").setExecutor(new BBSToperCommand());
		getCommand("bbstoper").setTabCompleter(new BBSToperCommand());

		new Reminder(this);
		new InventoryListener(this);

		Util.startAutoReward();
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			getLogger().info("Registering PlaceholderAPI expansion.");
			new PAPIExpansion().register();
		}

		new Metrics(this);
		this.getLogger().info("All tasks finished. Welcome to use BBSToper!");
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(INSTANCE);
		Thread thread = new Thread(() -> {
			Util.waitForAllTask();// 此方法会阻塞
			DatabaseManager.closeSQL();
			INSTANCE = null;
		});
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void saveResource(String resourcePath, boolean replace) {
		if(replace || !new File(getDataFolder(), resourcePath).exists()) // 我也不知道为什么Bukkit要丢一个警告出来
			super.saveResource(resourcePath, replace);
	}
}
