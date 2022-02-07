package it.polimi.mw.compinf.logging.spark.sink.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class to handle Database operations in spark using Hikari Conenction Pool
 */
public class DataSource {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        config.setJdbcUrl("jdbc:mysql://localhost:3306/task_logs");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername("dev");
        config.setPassword("dev");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    private DataSource() {
    }

    /**
     * Method to get a connection from the pool
     *
     * @return An open connection
     * @throws SQLException Error handling the connection
     */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
