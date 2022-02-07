package it.polimi.mw.compinf.logging.spark.sink;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.Serializable;

/**
 * Interface representing methods to use a sink for Spark Queries
 */
public interface SparkSinkInterface extends Serializable {

    public void completedPerMinute(Dataset<Row> batch, Long id);

    public void completedPerMonth(Dataset<Row> batch, Long id);

    public void completedPerWeek(Dataset<Row> batch, Long id);

    public void completedPerDay(Dataset<Row> batch, Long id);

    public void completedPerHour(Dataset<Row> batch, Long id);

    public void pendingTasks(Dataset<Row> batch, Long id);

    public void averageStartingTask(Dataset<Row> batch, Long id);
}
