package it.polimi.mw.compinf.logging.spark;

import it.polimi.mw.compinf.logging.LogUtils;
import it.polimi.mw.compinf.logging.spark.sink.SparkSinkInterface;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

/**
 * Class to handle Spark Streaming Queries using a SparkSinkInterface
 */
public class SparkStreamingWithSink implements SparkStreamingInterface {

    private Dataset<Row> starting;
    private Dataset<Row> pending;
    private Dataset<Row> completed;
    private List<DataStreamWriter<Row>> queries;
    private SparkSinkInterface sink;

    public SparkStreamingWithSink(SparkSinkInterface sink) {
        this.sink = sink;
        this.queries = new ArrayList<DataStreamWriter<Row>>();
    }


    /**
     * Method to set the streams coming from kafka topics
     */
    @Override
    public void setStreams(Dataset<Row> starting, Dataset<Row> pending, Dataset<Row> completed) {
        this.starting = starting;
        this.pending = pending;
        this.completed = completed;
    }

    /**
     * Method to build the List of queries requested in the List of enums
     */
    @Override
    public void buildQueries(List<SparkQueries> queries) {
        if (queries.contains(SparkQueries.AVERAGE_STARTING))
            this.queries.add(averageStart());
        if (queries.contains(SparkQueries.COMPLETED_PER_DAY))
            this.queries.add(completedPerDay());
        if (queries.contains(SparkQueries.COMPLETED_PER_HOUR))
            this.queries.add(completedPerHour());
        if (queries.contains(SparkQueries.COMPLETED_PER_MINUTE))
            this.queries.add(completedPerMinute());
        if (queries.contains(SparkQueries.COMPLETED_PER_MONTH))
            this.queries.add(completedPerMonth());
        if (queries.contains(SparkQueries.COMPLETED_PER_WEEK))
            this.queries.add(completedPerWeek());
        if (queries.contains(SparkQueries.PENDING_TASKS))
            this.queries.add(pendingTasks());

    }

    /**
     * Run all the queries built
     */
    @Override
    public List<StreamingQuery> runQueries() {
        //Disable Log messages
        LogUtils.setLogLevel();
        List<StreamingQuery> streamingQueries = new ArrayList<StreamingQuery>();
        queries.forEach((query) -> {
            try {
                streamingQueries.add(query.start());
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        });
        return streamingQueries;
    }

    @Override
    public void waitQueriesTermination(List<StreamingQuery> queries) {
        queries.forEach(t -> {
            try {
                t.awaitTermination();
            } catch (StreamingQueryException e) {
                e.printStackTrace();
            }
        });
    }

    //Personal query to see faster the updates
    private DataStreamWriter<Row> completedPerMinute() {
        return completed
                .withColumn("minute",
                        minute(col("timestamp")))
                .withColumn("hour",
                        hour(col("timestamp")))
                .withColumn("day",
                        to_date(col("timestamp"), "yyyy-MM-dd"))
                .groupBy(col("minute"), col("hour"), col("day"))
                .count()
                .writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("30 seconds"))
                .foreachBatch(sink::completedPerMinute)
                .queryName("Completed tasks per minute");
    }

    //Queries number 1
    private DataStreamWriter<Row> completedPerHour() {
        return completed
                .withColumn("hour",
                        hour(col("timestamp")))
                .withColumn("day",
                        to_date(col("timestamp"), "yyyy-MM-dd"))
                .groupBy(col("day"), col("hour"))
                .count()
                .writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(sink::completedPerHour)
                .queryName("Completed tasks per hour");
    }

    private DataStreamWriter<Row> completedPerDay() {
        return completed
                .withColumn("day",
                        to_date(col("timestamp"), "yyyy-MM-dd"))
                .groupBy(col("day"))
                .count()
                .writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(sink::completedPerDay)
                .queryName("Completed tasks per day");
    }

    private DataStreamWriter<Row> completedPerWeek() {
        return completed
                .withColumn("year",
                        year(col("timestamp")))
                .withColumn("week",
                        weekofyear(col("timestamp")))
                .groupBy(col("year"), col("week"))
                .count()
                .writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(sink::completedPerWeek)
                .queryName("Completed tasks per week");
    }

    private DataStreamWriter<Row> completedPerMonth() {
        return completed
                .withColumn("year",
                        year(col("timestamp")))
                .withColumn("month",
                        month(col("timestamp")))
                .groupBy(col("year"), col("month"))
                .count()
                .writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(sink::completedPerMonth)
                .queryName("Completed tasks per month");
    }

    //Query number 2
    private DataStreamWriter<Row> pendingTasks() {
        return pending
                .writeStream()
                .outputMode("complete")
                .trigger(Trigger.ProcessingTime("5 seconds"))
                .foreachBatch(sink::pendingTasks)
                .queryName("Pending Task");
    }

    //Query number 3
    private DataStreamWriter<Row> averageStart() {
        return starting
                .agg(expr("count(value)/approx_count_distinct(value)"))
                .withColumnRenamed("(count(value) / approx_count_distinct(value))", "average")
                .na().drop("any")
                .writeStream()
                .outputMode("complete")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(sink::averageStartingTask)
                .queryName("Average starting Task");
    }

}
