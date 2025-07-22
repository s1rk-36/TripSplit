package learn.tripsplit.data.mappers;

import learn.tripsplit.models.Expense;
import learn.tripsplit.models.User;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseMapper implements RowMapper<Expense> {

    @Override
    public Expense mapRow(ResultSet resultSet, int i) throws SQLException {
        Expense expense = new Expense();
        expense.setExpenseId(resultSet.getInt("expense_id"));
        expense.setGroupId(resultSet.getInt("group_id"));
        expense.setName(resultSet.getString("name"));
        expense.setTotalCost(resultSet.getBigDecimal("total_cost"));
        expense.setCategory(resultSet.getString("category"));
        expense.setDescription(resultSet.getString("description"));
        expense.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

        // Map created_by user - handle both cases (with and without user join)
        User createdBy = new User();
        createdBy.setUserId(resultSet.getInt("created_by"));

        // Check if user columns are available (when joined with user table)
        try {
            if (resultSet.getString("first_name") != null) {
                createdBy.setFirstName(resultSet.getString("first_name"));
                createdBy.setLastName(resultSet.getString("last_name"));
                createdBy.setEmail(resultSet.getString("email"));
            }
        } catch (SQLException e) {
            // No need to do anything
        }

        expense.setCreatedBy(createdBy);

        return expense;
    }
}