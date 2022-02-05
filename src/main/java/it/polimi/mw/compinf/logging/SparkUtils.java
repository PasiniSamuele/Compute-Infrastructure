package it.polimi.mw.compinf.logging;

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

import java.util.List;
import java.util.Map;


public class SparkUtils {

	public static JavaStreamingContext getStreamingContext() {
		// spark init
		final String master = "spark://127.0.0.1:7077";
		final SparkConf conf = new SparkConf().setMaster(master).setAppName("LogService");
		return new JavaStreamingContext(conf, Durations.seconds(1));
	}
	
	
	public static JavaInputDStream<ConsumerRecord<String, String>> getInputStream(List<String> topics, JavaStreamingContext sc,  Map<String, Object> props){
		return KafkaUtils.createDirectStream(
				sc,
				LocationStrategies.PreferConsistent(),
				ConsumerStrategies.<String, String> Subscribe(topics, props));
	}
	
	public static Dataset<Row> getStructuredStream(SparkSession spark, String topic, String kafkaServer, String watermark){
		return spark
				  .readStream()
				  .format("kafka")
				  .option("kafka.bootstrap.servers", kafkaServer)
				  .option("subscribe", topic)
				  .load()
				  .selectExpr("CAST(value AS STRING)","CAST(timestamp AS TIMESTAMP)")
				  .withWatermark("timestamp", watermark);
	}
	
	public static SparkSession getSession() {
		String master = "local[4]";

		return SparkSession
				.builder()
				.master(master)
				.appName("LogService")
				.getOrCreate()
				.newSession();
	}
}
