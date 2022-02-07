package it.polimi.mw.compinf.logging.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.streaming.StreamingQuery;

import java.util.List;

/**
 * Interface to manage Spark queries
 */
public interface SparkStreamingInterface {
    public void setStreams(Dataset<Row> starting, Dataset<Row> pending, Dataset<Row> completed);

    public void buildQueries(List<SparkQueries> queries);

    public List<StreamingQuery> runQueries();

    public void waitQueriesTermination(List<StreamingQuery> queries);
}
