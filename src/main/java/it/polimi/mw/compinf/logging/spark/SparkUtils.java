package it.polimi.mw.compinf.logging.spark;

import it.polimi.mw.compinf.logging.kafka.CustomKafkaUtils;
import it.polimi.mw.compinf.logging.spark.sink.DatabaseSQLSink;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.expr;

/**
 * Utils class to handle streams and Spark configuration
 */
public class SparkUtils {

    private final static String MASTER = "spark://127.0.0.1:7077";    //spark cluster address
    private final static String APP_NAME = "LogService";

    /**
     * Method to initilize the SparkSession
     *
     * @return New Spark Session
     */
    public static SparkSession getSession() {
        return SparkSession
                .builder()
                .master("local[4]")
                .appName(APP_NAME)
                .getOrCreate()
                .newSession();
    }

    /**
     * Reads a Structured Stream from a Kafka Topic
     *
     * @param spark       The Spark session
     * @param topic       Name of the Kafka topic
     * @param kafkaServer Address of the Kafka Server
     * @param watermark   Watermark for this stream
     * @return Dataset representing the Stream
     */
    public static Dataset<Row> getStructuredStream(SparkSession spark, String topic, String kafkaServer, String watermark) {
        return spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaServer)
                .option("subscribe", topic)
                .load()
                .selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)")
                .withWatermark("timestamp", watermark);
    }

    /**
     * Method to get the pending task stream without the completed ones
     *
     * @param starting starting tasks stream
     * @param pending  pending tasks stream
     * @return actual pending tasks stream
     */
    public static Dataset<Row> getActualPending(Dataset<Row> starting, Dataset<Row> pending) {
        return pending
                .union(starting)
                .groupBy("value")
                .count()
                .filter(expr("count<2"));
    }

    public static void setStreams(SparkSession sparkSession, SparkStreamingInterface sparkStreaming, String watermark) {

        //Kafka initialization
        String kafkaServer = CustomKafkaUtils.getServerAddr();


        //Get the streams from Kafka Topics
        Dataset<Row> completed = getStructuredStream(sparkSession, "completed", kafkaServer, watermark);
        Dataset<Row> pending = getStructuredStream(sparkSession, "pending", kafkaServer, watermark);
        Dataset<Row> starting = getStructuredStream(sparkSession, "starting", kafkaServer, watermark);

        // Pending task without the one already completed
        Dataset<Row> actualPending = SparkUtils.getActualPending(starting, pending);

        // Set the streams
        sparkStreaming.setStreams(starting, actualPending, completed);
    }

    public static SparkStreamingInterface getSparkStreaming() {
        return new SparkStreamingWithSink(new DatabaseSQLSink());
    }


}
