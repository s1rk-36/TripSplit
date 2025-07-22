package learn.tripsplit.data.mappers;

import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.UserExpense;
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
            AppUser appUser = new AppUser();
            appUser.setAppUserId(resultSet.getInt("user_id"));
            if (resultSet.getString("user_first_name") != null) {
                appUser.setFirstName(resultSet.getString("user_first_name"));
                appUser.setLastName(resultSet.getString("user_last_name"));
                appUser.setEmail(resultSet.getString("user_email"));
            }
            userExpense.setUser(appUser);
        }

        return userExpense;
    }
}