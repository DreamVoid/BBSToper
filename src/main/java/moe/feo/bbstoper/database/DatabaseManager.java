package moe.feo.bbstoper.database;

import moe.feo.bbstoper.BBSToper;
import moe.feo.bbstoper.config.Config;

public class DatabaseManager {
	public static AbstractDatabase database;

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
}
