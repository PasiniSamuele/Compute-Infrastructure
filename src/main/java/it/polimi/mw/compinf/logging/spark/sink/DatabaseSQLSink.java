package it.polimi.mw.compinf.logging.spark.sink;

import it.polimi.mw.compinf.logging.spark.sink.exceptions.NewTupleException;
import it.polimi.mw.compinf.logging.spark.sink.pool.DataSource;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.sql.*;
import java.time.Year;
import java.util.Iterator;
import java.util.Optional;

/**
 * Utils class to handle Database Sink in Spark
 */
public class DatabaseSQLSink implements SparkSinkInterface {

    public DatabaseSQLSink() {
    }


    //Sequence of methods to handle Queries of the problem number 1: completed per month, week,...
    //There is also the not requested "completed per minute" useful to test faster


    /**
     * Return the ID related to the searched ROW, if it is present
     *
     * @param st Statement with the Selection query
     * @return The ID eventually selected
     * @throws SQLException on failure
     */
    private Optional<Integer> getId(PreparedStatement st) throws SQLException {
        Optional<Integer> id = Optional.empty();
        try (ResultSet result = st.executeQuery()) {
            while (result.next()) {
                id = Optional.of(result.getInt(1));
            }
        }
        return id;
    }

    /**
     * Execute the update query or throw an exception in the id is null, so there is no tuple to update
     *
     * @param optionalId Id of the tuple tuple, if it exists
     * @param query      Update query to execute
     * @param connection Connection to database
     * @param completed  New value for the update
     * @throws SQLException      Error in the query execution
     * @throws NewTupleException The id is null, no tuple to update
     */
    private void updateCompletedTuple(Optional<Integer> optionalId, String query, Connection connection, Long completed) throws SQLException, NewTupleException {
        Integer id = optionalId.orElseThrow(NewTupleException::new);
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setLong(1, completed);
            st.setInt(2, id);
            st.executeUpdate();
        }
    }

    /**
     * Completed per Minute Query Database Sink
     */
    @Override
    public void completedPerMinute(Dataset<Row> batch, Long id) {

        batch.foreachPartition(new ForeachPartitionFunction<>() {
            private static final long serialVersionUID = 1L;

            public void call(Iterator<Row> t) {
                try (Connection connection = DataSource.getConnection()) {

                    while (t.hasNext()) {

                        //Extract every row in the batch

                        Row row = t.next();

                        //Get the fields
                        Date day = row.getDate(row.fieldIndex("day"));
                        int hour = row.getInt(row.fieldIndex("hour"));
                        int minute = row.getInt(row.fieldIndex("minute"));
                        long completed = row.getLong(row.fieldIndex("count"));


                        Optional<Integer> id;

                        //Get the id of the tuple to update (if present)
                        try (PreparedStatement st = connection.prepareStatement(Queries.SELECT_COMPLETED_BY_MINUTE)) {
                            st.setDate(1, day);
                            st.setInt(2, hour);
                            st.setInt(3, minute);
                            id = getId(st);
                        }

                        // Try to update the tuple in the database
                        try {
                            updateCompletedTuple(id, Queries.UPDATE_COMPLETED_BY_MINUTE, connection, completed);
                        }
                        //Handle the exception in case the tuple is not present and it is needed to create it
                        catch (NewTupleException e) {
                            try (PreparedStatement st = connection.prepareStatement(Queries.INSERT_COMPLETED_BY_MINUTE)) {
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

    /**
     * Completed per Month Query Database Sink
     */
    @Override
    public void completedPerMonth(Dataset<Row> batch, Long id) {
        batch.foreachPartition(new ForeachPartitionFunction<>() {
            private static final long serialVersionUID = 1L;

            public void call(Iterator<Row> t) {
                try (Connection connection = DataSource.getConnection()) {

                    while (t.hasNext()) {

                        //Extract every row in the batch
                        Row row = t.next();


                        //Get the fields
                        Year year = Year.of(row.getInt(row.fieldIndex("year")));
                        int month = row.getInt(row.fieldIndex("month"));
                        long completed = row.getLong(row.fieldIndex("count"));


                        Optional<Integer> id;

                        //Get the id of the tuple to update (if present)
                        try (PreparedStatement st = connection.prepareStatement(Queries.SELECT_COMPLETED_BY_MONTH)) {
                            st.setInt(1, year.getValue());
                            st.setInt(2, month);
                            id = getId(st);
                        }

                        // Try to update the tuple in the database
                        try {
                            updateCompletedTuple(id, Queries.UPDATE_COMPLETED_BY_MONTH, connection, completed);
                        }
                        //Handle the exception in case the tuple is not present and it is needed to create it
                        catch (NewTupleException e) {
                            try (PreparedStatement st = connection.prepareStatement(Queries.INSERT_COMPLETED_BY_MONTH)) {
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

    /**
     * Completed per Week Query Database Sink
     */
    @Override
    public void completedPerWeek(Dataset<Row> batch, Long id) {
        batch.foreachPartition(new ForeachPartitionFunction<>() {
            private static final long serialVersionUID = 1L;

            public void call(Iterator<Row> t) {
                try (Connection connection = DataSource.getConnection()) {

                    while (t.hasNext()) {

                        //Extract every row in the batch
                        Row row = t.next();

                        //Get the fields
                        Year year = Year.of(row.getInt(row.fieldIndex("year")));
                        int week = row.getInt(row.fieldIndex("week"));
                        long completed = row.getLong(row.fieldIndex("count"));

                        Optional<Integer> id;

                        //Get the id of the tuple to update (if present)
                        try (PreparedStatement st = connection.prepareStatement(Queries.SELECT_COMPLETED_BY_WEEK)) {
                            st.setInt(1, year.getValue());
                            st.setInt(2, week);
                            id = getId(st);
                        }

                        // Try to update the tuple in the database
                        try {
                            updateCompletedTuple(id, Queries.UPDATE_COMPLETED_BY_WEEK, connection, completed);
                        }
                        //Handle the exception in case the tuple is not present and it is needed to create it
                        catch (NewTupleException e) {
                            try (PreparedStatement st = connection.prepareStatement(Queries.INSERT_COMPLETED_BY_WEEK)) {
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

    /**
     * Completed per Day Query Database Sink
     */
    @Override
    public void completedPerDay(Dataset<Row> batch, Long id) {
        batch.foreachPartition(new ForeachPartitionFunction<>() {
            private static final long serialVersionUID = 1L;

            public void call(Iterator<Row> t) {
                try (Connection connection = DataSource.getConnection()) {

                    while (t.hasNext()) {

                        //Extract every row in the batch
                        Row row = t.next();

                        //Get the fields
                        Date day = row.getDate(row.fieldIndex("day"));
                        long completed = row.getLong(row.fieldIndex("count"));

                        Optional<Integer> id;

                        //Get the id of the tuple to update (if present)
                        try (PreparedStatement st = connection.prepareStatement(Queries.SELECT_COMPLETED_BY_DAY)) {
                            st.setDate(1, day);
                            id = getId(st);
                        }

                        // Try to update the tuple in the database
                        try {
                            updateCompletedTuple(id, Queries.UPDATE_COMPLETED_BY_DAY, connection, completed);
                        }
                        //Handle the exception in case the tuple is not present and it is needed to create it
                        catch (NewTupleException e) {
                            try (PreparedStatement st = connection.prepareStatement(Queries.INSERT_COMPLETED_BY_DAY)) {
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

    /**
     * Completed per Hour Query Database Sink
     */
    @Override
    public void completedPerHour(Dataset<Row> batch, Long id) {
        batch.foreachPartition(new ForeachPartitionFunction<>() {
            private static final long serialVersionUID = 1L;

            public void call(Iterator<Row> t) {
                try (Connection connection = DataSource.getConnection()) {

                    while (t.hasNext()) {

                        //Extract every row in the batch
                        Row row = t.next();

                        //Get the fields
                        Date day = row.getDate(row.fieldIndex("day"));
                        int hour = row.getInt(row.fieldIndex("hour"));
                        long completed = row.getLong(row.fieldIndex("count"));

                        Optional<Integer> id;

                        //Get the id of the tuple to update (if present)
                        try (PreparedStatement st = connection.prepareStatement(Queries.SELECT_COMPLETED_BY_HOUR)) {
                            st.setDate(1, day);
                            st.setInt(2, hour);
                            id = getId(st);
                        }

                        // Try to update the tuple in the database
                        try {
                            updateCompletedTuple(id, Queries.UPDATE_COMPLETED_BY_HOUR, connection, completed);
                        }
                        //Handle the exception in case the tuple is not present and it is needed to create it
                        catch (NewTupleException e) {
                            try (PreparedStatement st = connection.prepareStatement(Queries.INSERT_COMPLETED_BY_HOUR)) {
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

    /**
     * Pending Tasks Query Database Sink
     */
    @Override
    public void pendingTasks(Dataset<Row> batch, Long id) {
        long pendingCount = batch.count();
        try (Connection connection = DataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(Queries.INSERT_PENDING_COUNT)) {
            st.setLong(1, pendingCount);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Average Starting Task Query Database Sink
     */
    @Override
    public void averageStartingTask(Dataset<Row> batch, Long id) {

        batch.foreachPartition(new ForeachPartitionFunction<>() {
            private static final long serialVersionUID = 1L;

            public void call(Iterator<Row> t) {
                try (Connection connection = DataSource.getConnection()) {

                    while (t.hasNext()) {

                        //Extract every row in the batch (expected only one but it is a pattern)
                        Row row = t.next();

                        //Get the average
                        double average = row.getDouble(row.fieldIndex("average"));

                        //Insert into the database
                        try (PreparedStatement st = connection.prepareStatement(Queries.INSERT_AVERAGE_STARTS)) {
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
