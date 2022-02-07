package it.polimi.mw.compinf.logging.spark.sink;

/**
 * The SQL queries to execute in the database
 */
public class Queries {

    public static final String SELECT_COMPLETED_BY_MINUTE = "SELECT id FROM completed_by_minute WHERE day = ? AND hour = ? AND minute=?";
    public static final String INSERT_COMPLETED_BY_MINUTE = "INSERT INTO completed_by_minute (day, hour, minute, completed, last_update)  VALUES(?,?,?,?, CURRENT_TIMESTAMP())";
    public static final String UPDATE_COMPLETED_BY_MINUTE = "UPDATE completed_by_minute SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

    public static final String SELECT_COMPLETED_BY_HOUR = "SELECT id FROM completed_by_hour WHERE day = ? AND hour = ?";
    public static final String INSERT_COMPLETED_BY_HOUR = "INSERT INTO completed_by_hour (day, hour, completed, last_update)  VALUES(?,?,?, CURRENT_TIMESTAMP())";
    public static final String UPDATE_COMPLETED_BY_HOUR = "UPDATE completed_by_hour SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

    public static final String SELECT_COMPLETED_BY_DAY = "SELECT id FROM completed_by_day WHERE day = ?";
    public static final String INSERT_COMPLETED_BY_DAY = "INSERT INTO completed_by_day (day, completed, last_update)  VALUES(?,?, CURRENT_TIMESTAMP())";
    public static final String UPDATE_COMPLETED_BY_DAY = "UPDATE completed_by_day SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

    public static final String SELECT_COMPLETED_BY_WEEK = "SELECT id FROM completed_by_week WHERE year = ? AND week_of_year=?";
    public static final String INSERT_COMPLETED_BY_WEEK = "INSERT INTO completed_by_week (year, week_of_year, completed, last_update)  VALUES(?,?,?,CURRENT_TIMESTAMP())";
    public static final String UPDATE_COMPLETED_BY_WEEK = "UPDATE completed_by_week SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

    public static final String SELECT_COMPLETED_BY_MONTH = "SELECT id FROM completed_by_month WHERE year = ? AND month=?";
    public static final String INSERT_COMPLETED_BY_MONTH = "INSERT INTO completed_by_month (year, month, completed, last_update)  VALUES(?,?,?,CURRENT_TIMESTAMP())";
    public static final String UPDATE_COMPLETED_BY_MONTH = "UPDATE completed_by_month SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

    public static final String INSERT_PENDING_COUNT = "INSERT INTO pending_tasks (amount, timestamp) VALUES (?, CURRENT_TIMESTAMP)";

    public static final String INSERT_AVERAGE_STARTS = "INSERT INTO average_starts (average, timestamp) VALUES (?, CURRENT_TIMESTAMP)";
}
