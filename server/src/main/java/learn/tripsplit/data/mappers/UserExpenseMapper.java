package learn.tripsplit.data.mappers;

import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.UserExpense;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserExpenseMapper implements RowMapper<UserExpense> {

    // Fields
    private final AppUserMapper appUserMapper;
    private final ExpenseMapper expenseMapper;

    // Constructor
    public UserExpenseMapper(RoleFetcher roleFetcher) {
        this.appUserMapper = new AppUserMapper(roleFetcher);
        this.expenseMapper = new ExpenseMapper(roleFetcher);
    }

    // Mapper entry point used by Spring JDBC
    // Overload that delegates to main method
    @Override
    public UserExpense mapRow(ResultSet resultSet, int i) throws SQLException {
        return mapRow(resultSet, i, "", "", "", "", "", "");
    }

    public UserExpense mapRow(ResultSet resultSet, int i, String userExpensePrefix, String userPrefix, String expensePrefix, String expenseGroupPrefix, String expenseGroupCreatedByPrefix, String expenseCreatedByPrefix) throws SQLException {
        UserExpense userExpense = new UserExpense();
        userExpense.setUserId(resultSet.getInt(userExpensePrefix + "user_id"));
        userExpense.setExpenseId(resultSet.getInt(userExpensePrefix + "expense_id"));
        userExpense.setAmountOwed(resultSet.getBigDecimal(userExpensePrefix + "amount_owned"));
        userExpense.setAmountPaid(resultSet.getBigDecimal(userExpensePrefix + "amount_paid"));

        userExpense.setUser(appUserMapper.mapRow(resultSet, i, userPrefix));
        userExpense.setExpense(expenseMapper.mapRow(resultSet, i, expensePrefix, expenseGroupPrefix, expenseGroupCreatedByPrefix, expenseCreatedByPrefix));

        return userExpense;
    }
}