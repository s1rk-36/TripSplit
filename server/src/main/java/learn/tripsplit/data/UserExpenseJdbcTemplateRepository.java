package learn.tripsplit.data;

import learn.tripsplit.data.mappers.RoleFetcher;
import learn.tripsplit.data.mappers.UserExpenseMapper;
import learn.tripsplit.models.UserExpense;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class UserExpenseJdbcTemplateRepository implements UserExpenseRepository, RoleFetcher {

    private final JdbcTemplate jdbcTemplate;

    public UserExpenseJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<UserExpense> findAll() {
        final String sql = "select ue.user_id, ue.expense_id, ue.amount_owned, ue.amount_paid, "
                + "u.first_name as first_name, u.last_name as last_name, u.email as email "
                + "from user_expense ue "
                + "inner join user u on ue.user_id = u.user_id";
        return jdbcTemplate.query(sql, new UserExpenseMapper(this));
    }

    @Override
    public List<UserExpense> findByUserId(int userId) {
        final String sql = "select ue.user_id, ue.expense_id, ue.amount_owned, ue.amount_paid, "
                + "u.first_name as first_name, u.last_name as last_name, u.email as email "
                + "from user_expense ue "
                + "inner join user u on ue.user_id = u.user_id "
                + "where ue.user_id = ?";
        return jdbcTemplate.query(sql, new UserExpenseMapper(this), userId);
    }

    @Override
    public List<UserExpense> findByExpenseId(int expenseId) {
        final String sql = "SELECT ue.user_id, ue.expense_id, ue.amount_owned, ue.amount_paid, "
                + "u.first_name, u.last_name, u.email "
                + "FROM user_expense ue "
                + "INNER JOIN user u ON ue.user_id = u.user_id "
                + "WHERE ue.expense_id = ?;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserExpense userExpense = new UserExpense();
            userExpense.setUserId(rs.getInt("user_id"));
            userExpense.setExpenseId(rs.getInt("expense_id"));
            userExpense.setAmountOwed(rs.getBigDecimal("amount_owned"));
            userExpense.setAmountPaid(rs.getBigDecimal("amount_paid"));
            return userExpense;
        }, expenseId);
    }

    @Override
    public UserExpense findByUserIdAndExpenseId(int userId, int expenseId) {
        final String sql = "select ue.user_id, ue.expense_id, ue.amount_owned, ue.amount_paid, "
                + "u.first_name as first_name, u.last_name as last_name, u.email as email "
                + "from user_expense ue "
                + "inner join user u on ue.user_id = u.user_id "
                + "where ue.user_id = ? and ue.expense_id = ?";
        return jdbcTemplate.query(sql, new UserExpenseMapper(this), userId, expenseId).stream()
                .findFirst().orElse(null);
    }

    @Override
    public UserExpense add(UserExpense userExpense) {
        final String sql = "insert into user_expense (user_id, expense_id, amount_owned, amount_paid) "
                + "values (?,?,?,?)";

        int rowsAffected = jdbcTemplate.update(sql,
                userExpense.getUserId(),
                userExpense.getExpenseId(),
                userExpense.getAmountOwed(),
                userExpense.getAmountPaid());

        if (rowsAffected <= 0) {
            return null;
        }

        return userExpense;
    }

    @Override
    public boolean update(UserExpense userExpense) {
        final String sql = "update user_expense set "
                + "amount_owned = ?, "
                + "amount_paid = ? "
                + "where user_id = ? and expense_id = ?";

        return jdbcTemplate.update(sql,
                userExpense.getAmountOwed(),
                userExpense.getAmountPaid(),
                userExpense.getUserId(),
                userExpense.getExpenseId()) > 0;
    }

    @Override
    public boolean deleteByUserIdAndExpenseId(int userId, int expenseId) {
        return jdbcTemplate.update("delete from user_expense where user_id = ? and expense_id = ?",
                userId, expenseId) > 0;
    }

    @Override
    public List<String> getRolesByAppUserId(int userId) {
        final String sql = "select r.name "
                + "from user_role ur "
                + "inner join role r on ur.role_id = r.role_id "
                + "where ur.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"), userId);
    }

}
