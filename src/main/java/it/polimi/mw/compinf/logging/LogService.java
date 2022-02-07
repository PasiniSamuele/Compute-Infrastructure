package it.polimi.mw.compinf.logging;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;

import it.polimi.mw.compinf.logging.spark.SparkQueries;
import it.polimi.mw.compinf.logging.spark.SparkStreamingInterface;
import it.polimi.mw.compinf.logging.spark.SparkUtils;



public class LogService {
	
	private static final String WATERMARK = "1 hour";
	
	private static final List<SparkQueries> SPARK_QUERIES= Arrays.asList(		
					SparkQueries.COMPLETED_PER_MINUTE,
					SparkQueries.COMPLETED_PER_HOUR,
					SparkQueries.COMPLETED_PER_DAY,
					SparkQueries.COMPLETED_PER_WEEK,
					SparkQueries.COMPLETED_PER_MONTH,
					SparkQueries.AVERAGE_STARTING,
					SparkQueries.PENDING_TASKS	        
			);

	public static void main(String[] args) throws TimeoutException {
		
		//Disable Log messages
		LogUtils.setLogLevel();
		
		//Spark initialization
		SparkSession sparkSession = SparkUtils.getSession();
		SparkStreamingInterface sparkStreaming = SparkUtils.getSparkStreaming();
		
		//Initialize the Streams
		SparkUtils.setStreams(sparkSession, sparkStreaming, WATERMARK);

		//Create the queries
		sparkStreaming.buildQueries(SPARK_QUERIES);
		
		//Run the queries
		List<StreamingQuery> queries = sparkStreaming.runQueries();
		

		//Termination
		sparkStreaming.waitQueriesTermination(queries);
		sparkSession.close();
	}
	
	

}
