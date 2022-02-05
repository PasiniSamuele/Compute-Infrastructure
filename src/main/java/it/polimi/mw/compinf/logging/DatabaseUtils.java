package it.polimi.mw.compinf.logging;

import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.sql.*;
import java.time.Year;
import java.util.Iterator;

public class DatabaseUtils {

	private static final String SELECT_COMPLETED_BY_MINUTE = "SELECT id FROM completed_by_minute WHERE day = ? AND hour = ? AND minute=?";
	private static final String INSERT_COMPLETED_BY_MINUTE = "INSERT INTO completed_by_minute (day, hour, minute, completed, last_update)  VALUES(?,?,?,?, CURRENT_TIMESTAMP())";
	private static final String UPDATE_COMPLETED_BY_MINUTE = "UPDATE completed_by_minute SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

	private static final String SELECT_COMPLETED_BY_HOUR = "SELECT id FROM completed_by_hour WHERE day = ? AND hour = ?";
	private static final String INSERT_COMPLETED_BY_HOUR = "INSERT INTO completed_by_hour (day, hour, completed, last_update)  VALUES(?,?,?, CURRENT_TIMESTAMP())";
	private static final String UPDATE_COMPLETED_BY_HOUR = "UPDATE completed_by_hour SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

	private static final String SELECT_COMPLETED_BY_DAY = "SELECT id FROM completed_by_day WHERE day = ?";
	private static final String INSERT_COMPLETED_BY_DAY = "INSERT INTO completed_by_day (day, completed, last_update)  VALUES(?,?, CURRENT_TIMESTAMP())";
	private static final String UPDATE_COMPLETED_BY_DAY = "UPDATE completed_by_day SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

	private static final String SELECT_COMPLETED_BY_WEEK = "SELECT id FROM completed_by_week WHERE year = ? AND week_of_year=?";
	private static final String INSERT_COMPLETED_BY_WEEK = "INSERT INTO completed_by_week (year, week_of_year, completed, last_update)  VALUES(?,?,?,CURRENT_TIMESTAMP())";
	private static final String UPDATE_COMPLETED_BY_WEEK = "UPDATE completed_by_week SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

	private static final String SELECT_COMPLETED_BY_MONTH = "SELECT id FROM completed_by_month WHERE year = ? AND month=?";
	private static final String INSERT_COMPLETED_BY_MONTH = "INSERT INTO completed_by_month (year, month, completed, last_update)  VALUES(?,?,?,CURRENT_TIMESTAMP())";
	private static final String UPDATE_COMPLETED_BY_MONTH = "UPDATE completed_by_month SET completed =  ? , last_update = CURRENT_TIMESTAMP() WHERE id=?";

	private static final String INSERT_PENDING_COUNT = "INSERT INTO pending_tasks (amount, timestamp) VALUES (?, CURRENT_TIMESTAMP)";

	private static final String INSERT_AVERAGE_STARTS = "INSERT INTO average_starts (average, timestamp) VALUES (?, CURRENT_TIMESTAMP)";


	public static void completedPerMinute(Dataset<Row> batch, Long id) {
		batch.foreachPartition(new ForeachPartitionFunction<Row>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void call(Iterator<Row> t) throws Exception {
				try (Connection connection = DataSource.getConnection()) {

					while (t.hasNext()) {

						Row row = t.next(); // extract every row in the batch

						int indexDay = row.fieldIndex("day");
						Date day = row.getDate(indexDay);

						int indexHour = row.fieldIndex("hour");
						int hour = row.getInt(indexHour);

						int indexMinute = row.fieldIndex("minute");
						int minute = row.getInt(indexMinute);

						int indexCompleted = row.fieldIndex("count");
						long completed = row.getLong(indexCompleted);

						int count = 0;
						int id = 0;
						System.out.println("Per Minute: " + completed + " at day " + day.toLocalDate().toString()
								+ " at hour " + hour + " at minute " + minute);

						try (PreparedStatement st = connection.prepareStatement(SELECT_COMPLETED_BY_MINUTE)) {
							st.setDate(1, day);
							st.setInt(2, hour);
							st.setInt(3, minute);
							try (ResultSet result = st.executeQuery()) {
								while (result.next()) {
									id = result.getInt(1);
									System.out.println("found with id " + id);
									count++;
								}
							}
						}

						if (count > 0) { // to update
							try (PreparedStatement st = connection.prepareStatement(UPDATE_COMPLETED_BY_MINUTE)) {
								System.out.println("updating id " + id + " with completed " + completed);
								st.setLong(1, completed);
								st.setInt(2, id);
								st.executeUpdate();
							}
						} else {
							try (PreparedStatement st = connection.prepareStatement(INSERT_COMPLETED_BY_MINUTE)) {
								st.setDate(1, day);
								st.setInt(2, hour);
								st.setInt(3, minute);
								st.setLong(4, completed);
								st.executeUpdate();
							}
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		});

	}

	public static void completedPerMonth(Dataset<Row> batch, Long id) {
		batch.show();
		System.out.println("cane");
		batch.foreachPartition(new ForeachPartitionFunction<Row>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void call(Iterator<Row> t) throws Exception {
				try (Connection connection = DataSource.getConnection()) {

					while (t.hasNext()) {

						System.out.println("row found");
						Row row = t.next(); // extract every row in the batch

						int indexYear = row.fieldIndex("year");
						Year year = Year.of(row.getInt(indexYear));

						int indexMonth = row.fieldIndex("month");
						int month = row.getInt(indexMonth);

						int indexCompleted = row.fieldIndex("count");
						long completed = row.getLong(indexCompleted);

						int count = 0;
						int id = 0;
						System.out.println(
								"Per Month: " + completed + " at year " + year.getValue() + " at month " + month);

						try (PreparedStatement st = connection.prepareStatement(SELECT_COMPLETED_BY_MONTH)) {
							st.setInt(1, year.getValue());
							st.setInt(2, month);
							try (ResultSet result = st.executeQuery()) {
								while (result.next()) {
									id = result.getInt(1);
									count++;
								}
							}
						}

						if (count > 0) { // to update
							try (PreparedStatement st = connection.prepareStatement(UPDATE_COMPLETED_BY_MONTH)) {
								st.setLong(1, completed);
								st.setInt(2, id);
								st.executeUpdate();
							}
						} else {
							try (PreparedStatement st = connection.prepareStatement(INSERT_COMPLETED_BY_MONTH)) {
								st.setInt(1, year.getValue());
								st.setInt(2, month);
								st.setLong(3, completed);
								st.executeUpdate();
							}
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		});
	}

	public static void completedPerWeek(Dataset<Row> batch, Long id) {
		batch.foreachPartition(new ForeachPartitionFunction<Row>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void call(Iterator<Row> t) throws Exception {
				try (Connection connection = DataSource.getConnection()) {

					while (t.hasNext()) {

						Row row = t.next(); // extract every row in the batch

						int indexYear = row.fieldIndex("year");
						Year year = Year.of(row.getInt(indexYear));

						int indexWeek = row.fieldIndex("week");
						int week = row.getInt(indexWeek);

						int indexCompleted = row.fieldIndex("count");
						long completed = row.getLong(indexCompleted);

						int count = 0;
						int id = 0;

						System.out
								.println("Per Week: " + completed + " at year " + year.getValue() + " at week " + week);

						try (PreparedStatement st = connection.prepareStatement(SELECT_COMPLETED_BY_WEEK)) {
							st.setInt(1, year.getValue());
							st.setInt(2, week);
							try (ResultSet result = st.executeQuery()) {
								while (result.next()) {
									id = result.getInt(1);
									count++;
								}
							}
						}

						if (count > 0) { // to update
							try (PreparedStatement st = connection.prepareStatement(UPDATE_COMPLETED_BY_WEEK)) {
								st.setLong(1, completed);
								st.setInt(2, id);
								st.executeUpdate();
							}
						} else {
							try (PreparedStatement st = connection.prepareStatement(INSERT_COMPLETED_BY_WEEK)) {
								st.setInt(1, year.getValue());
								st.setInt(2, week);
								st.setLong(3, completed);
								st.executeUpdate();
							}
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		});
	}

	public static void completedPerDay(Dataset<Row> batch, Long id) {
		batch.foreachPartition(new ForeachPartitionFunction<Row>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void call(Iterator<Row> t) throws Exception {
				try (Connection connection = DataSource.getConnection()) {

					while (t.hasNext()) {

						Row row = t.next(); // extract every row in the batch

						int indexDay = row.fieldIndex("day");
						Date day = row.getDate(indexDay);

						int indexCompleted = row.fieldIndex("count");
						long completed = row.getLong(indexCompleted);

						int count = 0;
						int id = 0;

						System.out.println("Per Day: " + completed + " at day " + day.toLocalDate().toString());

						try (PreparedStatement st = connection.prepareStatement(SELECT_COMPLETED_BY_DAY)) {
							st.setDate(1, day);

							try (ResultSet result = st.executeQuery()) {
								while (result.next()) {
									id = result.getInt(1);
									System.out.println("found with id " + id);
									count++;
								}
							}
						}

						if (count > 0) { // to update
							try (PreparedStatement st = connection.prepareStatement(UPDATE_COMPLETED_BY_DAY)) {
								System.out.println("updating id " + id + " with completed " + completed);
								st.setLong(1, completed);
								st.setInt(2, id);
								st.executeUpdate();
							}
						} else {
							try (PreparedStatement st = connection.prepareStatement(INSERT_COMPLETED_BY_DAY)) {
								st.setDate(1, day);
								st.setLong(2, completed);
								st.executeUpdate();
							}
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		});
	}

	public static void completedPerHour(Dataset<Row> batch, Long id) {
		batch.foreachPartition(new ForeachPartitionFunction<Row>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void call(Iterator<Row> t) throws Exception {
				try (Connection connection = DataSource.getConnection()) {

					while (t.hasNext()) {

						Row row = t.next(); // extract every row in the batch

						int indexDay = row.fieldIndex("day");
						Date day = row.getDate(indexDay);

						int indexHour = row.fieldIndex("hour");
						int hour = row.getInt(indexHour);

						int indexCompleted = row.fieldIndex("count");
						long completed = row.getLong(indexCompleted);

						System.out.println("Per Hour: " + completed + " at day " + day.toLocalDate().toString()
								+ " at hour " + hour);
						int count = 0;
						int id = 0;

						try (PreparedStatement st = connection.prepareStatement(SELECT_COMPLETED_BY_HOUR)) {
							st.setDate(1, day);
							st.setInt(2, hour);
							try (ResultSet result = st.executeQuery()) {
								while (result.next()) {
									id = result.getInt(1);
									count++;
								}
							}
						}

						if (count > 0) { // to update
							try (PreparedStatement st = connection.prepareStatement(UPDATE_COMPLETED_BY_HOUR)) {
								st.setLong(1, completed);
								st.setInt(2, id);
								st.executeUpdate();
							}
						} else {
							try (PreparedStatement st = connection.prepareStatement(INSERT_COMPLETED_BY_HOUR)) {
								st.setDate(1, day);
								st.setInt(2, hour);
								st.setLong(3, completed);
								st.executeUpdate();
							}
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		});
	}

	public static void pendingTasks(Dataset<Row> batch, Long id) {
		long pendingCount = batch.count();
		try (Connection connection = DataSource.getConnection();
				PreparedStatement st = connection.prepareStatement(INSERT_PENDING_COUNT)) {
			st.setLong(1, pendingCount);
			st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void averageStartingTask(Dataset<Row> batch, Long id) {
		batch.foreachPartition(new ForeachPartitionFunction<Row>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void call(Iterator<Row> t) throws Exception {
				try (Connection connection = DataSource.getConnection()) {

					while (t.hasNext()) {

						Row row = t.next(); // extract every row in the batch (expected only one but it is a pattern)

						int indexAvg = row.fieldIndex("average");
						Double average = row.getDouble(indexAvg);
						try (PreparedStatement st = connection.prepareStatement(INSERT_AVERAGE_STARTS)) {
							st.setDouble(1, average);
							st.executeUpdate();
						}

					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		});

	}

}
