package moe.feo.bbstoper.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.dreamvoid.bbstoper.Utils;
import moe.feo.bbstoper.BBSToper;
import moe.feo.bbstoper.config.Config;
import moe.feo.bbstoper.config.Message;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL extends AbstractDatabase {
	private HikariDataSource ds;

	@Override
	protected void connect() throws ClassNotFoundException {
		String driver;
		if(Utils.findClass("com.mysql.cj.jdbc.Driver")){
			driver = "com.mysql.cj.jdbc.Driver";
		} else if (Utils.findClass("com.mysql.jdbc.Driver")) {
			driver = "com.mysql.jdbc.Driver";
		} else throw new ClassNotFoundException("Both \"com.mysql.jdbc.Driver\" and \"com.mysql.cj.jdbc.Driver\" not found.");

		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driver);
		config.setPoolName("BBSToper MySQL");
		config.setJdbcUrl("jdbc:mysql://" + Config.DATABASE_MYSQL_ADDRESS + "/" + Config.DATABASE_MYSQL_DATABASE + Config.DATABASE_MYSQL_PARAMETERS);
		config.setUsername(Config.DATABASE_MYSQL_USERNAME.getString());
		config.setPassword(Config.DATABASE_MYSQL_PASSWORD.getString());
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
			BBSToper.INSTANCE.getLogger().severe(Message.SQL_EXCEPTION.getString());
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void closeConnection() {
		ds.close();
	}
}
