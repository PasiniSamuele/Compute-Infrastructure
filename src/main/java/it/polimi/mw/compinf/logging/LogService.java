package it.polimi.mw.compinf.logging;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.hour;
import static org.apache.spark.sql.functions.to_date;

import java.util.concurrent.TimeoutException;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;


public class LogService {

	public static void main(String[] args) throws TimeoutException {
		
		LogUtils.setLogLevel();
		
		//spark init
		SparkSession spark = SparkUtils.getSession();  
        
        //kafka inint
		String kafkaServer = CustomKafkaUtils.getServerAddr();
        
		
		Dataset<Row> completed = SparkUtils.getStructuredStream(spark, "completed", kafkaServer);
	
		StreamingQuery completedPerHour = completed
				.selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)")
				.withColumn("day",
					    to_date(col("timestamp"),"yyyy-MM-dd"))
				.withColumn("hour", 
						hour(col("timestamp")))
				.groupBy(col("day"),col("hour"))
				.count()
				.writeStream()
                .outputMode("complete")
                .format("console")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .queryName("Completed tasks per Hour")
                .start();
		
		
		/*StreamingQuery completedPerDay = completed
				.selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)")
				.withColumn("day",
					    to_date(col("timestamp"),"yyyy-MM-dd"))
				.groupBy(col("day"))
				.count()
				.writeStream()
                .outputMode("complete")
                .format("console")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .queryName("Completed tasks per day")
                .start();
		
		StreamingQuery completedPerWeek = completed
				.selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)")
				.withColumn("year",
					    year(col("timestamp")))
				.withColumn("week", 
						weekofyear(col("timestamp")))
				.groupBy(col("year"), col("week"))
				.count()
				.writeStream()
                .outputMode("complete")
                .format("console")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .queryName("Completed tasks per week")
                .start();
		
		StreamingQuery completedPerMonth = completed
				.selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)")
				.withColumn("year",
					    year(col("timestamp")))
				.withColumn("month", 
						month(col("timestamp")))
				.groupBy(col("year"), col("month"))
				.count()
				.writeStream()
                .outputMode("complete")
                .format("console")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .queryName("Completed tasks per month")
                .start();*/

	
		try {
			completedPerHour.awaitTermination();
			/*completedPerDay.awaitTermination();
			completedPerWeek.awaitTermination();
			completedPerMonth.awaitTermination();*/
			
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }


        spark.close();
	}

}
