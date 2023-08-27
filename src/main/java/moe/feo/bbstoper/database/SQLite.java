package moe.feo.bbstoper.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.dreamvoid.bbstoper.Utils;
import moe.feo.bbstoper.BBSToper;
import moe.feo.bbstoper.config.Config;
import moe.feo.bbstoper.config.Message;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLite extends AbstractDatabase {
	private HikariDataSource ds;

	@Override
	protected void connect() throws ClassNotFoundException {
		String driver;
		if(Utils.findClass("org.sqlite.JDBC")){
			driver = "org.sqlite.JDBC";
		} else throw new ClassNotFoundException("\"org.sqlite.JDBC\" not found.");

		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driver);
		config.setPoolName("BBSToper SQLite");
		config.setJdbcUrl("jdbc:sqlite:" + Config.DATABASE_SQLITE_FOLDER.getString().replaceAll("%PLUGIN_FOLDER%", "%s") + File.separator + Config.DATABASE_SQLITE_DATABASE.getString());
		config.setConnectionTimeout(Config.DATABASE_POOL_CONNECTIONTIMEOUT.getLong());
		config.setIdleTimeout(Config.DATABASE_POOL_IDLETIMEOUT.getLong());
		config.setMaxLifetime(Config.DATABASE_POOL_MAXLIFETIME.getLong());
		config.setMaximumPoolSize(Config.DATABASE_POOL_MAXIUMPOOLSIZE.getInt());
		config.setKeepaliveTime(Config.DATABASE_POOL_KEEPALIVETIME.getLong());
		config.setMinimumIdle(Config.DATABASE_POOL_MINIMUMIDLE.getInt());
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

		ds = new HikariDataSource(config);
	}

	@Override
	protected Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			BBSToper.INSTANCE.getLogger().severe(Message.FAILEDCONNECTSQL.getString());
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void closeConnection() {
		ds.close();
	}
}
