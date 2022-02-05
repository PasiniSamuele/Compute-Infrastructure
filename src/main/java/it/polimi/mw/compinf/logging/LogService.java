package it.polimi.mw.compinf.logging;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;

import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;



public class LogService {

	public static void main(String[] args) throws TimeoutException {
		
		LogUtils.setLogLevel();
		
		//spark init
		SparkSession sparkSession = SparkUtils.getSession();

        //kafka inint
		String kafkaServer = CustomKafkaUtils.getServerAddr();
        
		
		//get the log stream
		Dataset<Row> completed = SparkUtils.getStructuredStream(sparkSession, "completed", kafkaServer, "1 hour");
		
		Dataset<Row> pending = SparkUtils.getStructuredStream(sparkSession, "pending", kafkaServer, "1 hour");
		
		Dataset<Row> starting = SparkUtils.getStructuredStream(sparkSession, "starting", kafkaServer, "1 hour");

		// Query number 2
		Dataset<Row> actualPending = pending
				.union(starting)
				.groupBy("value")
				.count()
				.filter(expr("count<2"));

		StreamingQuery pendingTasks = actualPending
				.writeStream()
				.outputMode("complete")
				.trigger(Trigger.ProcessingTime("5 seconds"))
				.foreachBatch(DatabaseUtils::pendingTasks)
				.queryName("Pending Task")
				.start();

		//Queries number 1
		StreamingQuery completedPerHour = completed
				.withColumn("hour", 
						hour(col("timestamp")))
				.withColumn("day",
					    to_date(col("timestamp"),"yyyy-MM-dd"))
				.groupBy(col("day"), col("hour"))
				.count()
				.writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(DatabaseUtils::completedPerHour)
                .queryName("Completed tasks per hour")
                .start();
		
		StreamingQuery completedPerDay = completed
				.withColumn("day",
					    to_date(col("timestamp"),"yyyy-MM-dd"))
				.groupBy(col("day"))
				.count()
				.writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(DatabaseUtils::completedPerDay)
                .queryName("Completed tasks per day")
                .start();
		
		StreamingQuery completedPerWeek = completed
				.withColumn("year",
					    year(col("timestamp")))
				.withColumn("week", 
						weekofyear(col("timestamp")))
				.groupBy(col("year"), col("week"))
				.count()
				.writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(DatabaseUtils::completedPerWeek)
                .queryName("Completed tasks per week")
                .start();
		

		StreamingQuery completedPerMonth = completed
				.withColumn("year",
					    year(col("timestamp")))
				.withColumn("month", 
						month(col("timestamp")))
				.groupBy(col("year"), col("month"))
				.count()
				.writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(DatabaseUtils::completedPerMonth)
                .queryName("Completed tasks per month")
                .start();

		//Personal query to see faster the updates
		StreamingQuery completedPerMinute = completed
				.withColumn("minute",
					    minute(col("timestamp")))
				.withColumn("hour",
					   	hour(col("timestamp")))
				.withColumn("day",
					    to_date(col("timestamp"),"yyyy-MM-dd"))
				.groupBy(col("minute"), col("hour"), col("day"))
				.count()
				.writeStream()
                .outputMode("update")
                .trigger(Trigger.ProcessingTime("30 seconds"))
                .foreachBatch(DatabaseUtils::completedPerMinute)
                .queryName("Completed tasks per minute")
                .start();


		/*SparkContext sparkContext = sparkSession.sparkContext();

		Runnable runPendQuery = () -> {
			sparkContext.setLocalProperty("spark.scheduler.pool", "high");

			// Query number 2
			Dataset<Row> actualPending = pending
					.union(starting)
					.groupBy("value")
					.count()
					.filter(expr("count<2"));

			try {
				StreamingQuery pendingTasks = actualPending
						.writeStream()
						.outputMode("complete")
						.trigger(Trigger.ProcessingTime("5 seconds"))
						.foreachBatch(DatabaseUtils::pendingTasks)
						.queryName("Pending Task")
						.start();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		};

		Thread thPendQuery = new Thread(runPendQuery);
		thPendQuery.start();*/

		
		//Query number 3
		StreamingQuery averageStart = starting
				.agg(expr("count(value)/approx_count_distinct(value)"))
				.withColumnRenamed("(count(value) / approx_count_distinct(value))", "average")
				.na().drop("any")
				.writeStream()
				.outputMode("complete")
				.trigger(Trigger.ProcessingTime("1 minute"))
                .foreachBatch(DatabaseUtils::averageStartingTask)
                .queryName("Average starting Task")
                .start();
		
		try {
			completedPerMinute.awaitTermination();
			completedPerMonth.awaitTermination();
			completedPerDay.awaitTermination();
			completedPerWeek.awaitTermination();
			completedPerHour.awaitTermination();
			pendingTasks.awaitTermination();
			//thPendQuery.join();
			averageStart.awaitTermination();
			
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }


		sparkSession.close();
	}

}
