package learn.tripsplit.data.mappers;

import learn.tripsplit.models.UserExpense;
import learn.tripsplit.models.User;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserExpenseMapper implements RowMapper<UserExpense> {

    @Override
    public UserExpense mapRow(ResultSet resultSet, int i) throws SQLException {
        UserExpense userExpense = new UserExpense();
        userExpense.setId(resultSet.getInt("id"));
        userExpense.setUserId(resultSet.getInt("user_id"));
        userExpense.setExpenseId(resultSet.getInt("expense_id"));
        userExpense.setAmountOwed(resultSet.getBigDecimal("amount_owed"));
        userExpense.setAmountPaid(resultSet.getBigDecimal("amount_paid"));

        // Map user if available
        if (resultSet.getMetaData().getColumnCount() > 5) {
            User user = new User();
            user.setUserId(resultSet.getInt("user_id"));
            if (resultSet.getString("user_first_name") != null) {
                user.setFirstName(resultSet.getString("user_first_name"));
                user.setLastName(resultSet.getString("user_last_name"));
                user.setEmail(resultSet.getString("user_email"));
            }
            userExpense.setUser(user);
        }

        return userExpense;
    }
}