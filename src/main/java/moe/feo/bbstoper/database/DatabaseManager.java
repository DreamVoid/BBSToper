package moe.feo.bbstoper.database;

import moe.feo.bbstoper.BBSToper;
import moe.feo.bbstoper.utils.Util;
import moe.feo.bbstoper.config.Config;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DatabaseManager {
	public static AbstractDatabase database;
	private static BukkitTask timingReconnectTask;

	public static void initializeDatabase() {// 初始化或重载数据库
		try {
			AbstractDatabase.writelock.lock();
			if (database != null) database.closeConnection();// 此方法会在已经建立过连接的情况下关闭连接

			switch (Config.DATABASE_TYPE.getString().toLowerCase()){
				case "sqlite":
				default:{
					database = new SQLite();
					break;
				}
				case "mysql":{
					database = new MySQL();
					break;
				}
			}
			database.connect();
			database.createTablePosters();
			database.createTableTopStates();
		} catch (Exception e) {
			BBSToper.INSTANCE.getLogger().severe("Failed to initialize database: " + e);
			if(Config.DEBUG.getBoolean()) e.printStackTrace();
		} finally {
			AbstractDatabase.writelock.unlock();
		}
	}

	public static void closeSQL() {// 关闭数据库
		database.closeConnection();
		database = null;
	}

	public static void startTimingReconnect() {// 自动重连数据库的方法
		if (timingReconnectTask != null && !timingReconnectTask.isCancelled()) {// 将之前的任务取消(如果存在)
			timingReconnectTask.cancel();
		}
		int period = Config.DATABASE_TIMINGRECONNECT.getInt() * 20;
		if (period > 0) {
			timingReconnectTask = new BukkitRunnable() {
				@Override
				public void run() {
					Util.addRunningTaskID(this.getTaskId());
					initializeDatabase();// 重载数据库
					Util.removeRunningTaskID(this.getTaskId());
				}
			}.runTaskTimerAsynchronously(BBSToper.INSTANCE, period, period);
		}
	}
}
