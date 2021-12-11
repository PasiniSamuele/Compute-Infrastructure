package it.polimi.middleware.project3;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

public class SparkUtils {

	public final static JavaStreamingContext getStreamingContext() {
		// spark init
		final String master = "local[4]";
		final SparkConf conf = new SparkConf().setMaster(master).setAppName("LogService");
		return new JavaStreamingContext(conf, Durations.seconds(1));
	}
	
	
	public final static JavaInputDStream<ConsumerRecord<String, String>> getInputStream(List<String> topics, JavaStreamingContext sc,  Map<String, Object> props){
		JavaInputDStream<ConsumerRecord<String, String>> stream = 
      		  KafkaUtils.createDirectStream(
      				  sc, 
      		    LocationStrategies.PreferConsistent(), 
      		    ConsumerStrategies.<String, String> Subscribe(topics, props));
		return stream;
	}
	
	public final static Dataset<Row> getStructuredStream(SparkSession spark, String topic, String kafkaServer){
		Dataset<Row> df = spark
				  .readStream()
				  .format("kafka")
				  .option("kafka.bootstrap.servers", kafkaServer)
				  .option("subscribe", topic)
				  .load();
				 // .selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)");
		return df;
	}
	
	public final static SparkSession getSession() {
		String master = "local[4]";

        SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("LogService")
                .getOrCreate()
                .newSession();
        return spark;
	}
}
