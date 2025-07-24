package learn.tripsplit.data;

import learn.tripsplit.data.mappers.*;
import learn.tripsplit.models.Category;
import learn.tripsplit.models.Expense;
import learn.tripsplit.models.UserExpense;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ExpenseJdbcTemplateRepository implements ExpenseRepository, RoleFetcher {

    private final JdbcTemplate jdbcTemplate;

    public ExpenseJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Expense> findAll() {
        final String sql = "select "
                + "e.expense_id, "
                + "e.`name` as expense_name, "
                + "e.total_cost, "
                + "e.category, "
                + "e.`description` as expense_description, "
                + "e.created_at, "

                + "g.group_id, "
                + "g.`name` as group_name, "
                + "g.`description` as group_description, "

                + "gcb.user_id as gcb_user_id, "
                + "gcb.first_name as gcb_first_name, "
                + "gcb.last_name as gcb_last_name, "
                + "gcb.email as gcb_email, "
                + "gcb.username as gcb_username, "
                + "gcb.password_hash as gcb_password_hash, "
                + "gcb.disabled as gcb_disabled, "

                + "ecb.user_id as ecb_user_id, "
                + "ecb.first_name as ecb_first_name, "
                + "ecb.last_name as ecb_last_name, "
                + "ecb.email as ecb_email, "
                + "ecb.username as ecb_username, "
                + "ecb.password_hash as ecb_password_hash, "
                + "ecb.disabled as ecb_disabled "

                + "from expense e "
                + "inner join `group` as g on e.group_id = g.group_id "
                + "inner join `user` as gcb on g.created_by = gcb.user_id "
                + "inner join `user` as ecb on e.created_by = ecb.user_id "
                + "limit 1000;";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new ExpenseMapper(this)
                        .mapRow(rs, rowNum, "", "", "gcb_", "ecb_")
        );
    }

    @Override
    public List<Expense> findByGroupId(int groupId) {
        final String sql = "SELECT e.expense_id, e.name, e.total_cost, e.category, e.description, e.created_at, e.created_by, "
                + "u.first_name, u.last_name, u.email "
                + "from expense e "
                + "inner join user u on e.created_by = u.user_id "
                + "where e.group_id = ? "
                + "order by e.created_at desc;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Expense expense = new Expense();
            expense.setExpenseId(rs.getInt("expense_id"));
            expense.setName(rs.getString("name"));
            expense.setTotalCost(rs.getBigDecimal("total_cost"));
            expense.setCategory(Category.valueOf(rs.getString("category")));
            expense.setDescription(rs.getString("description"));
            expense.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            expense.setGroupId(groupId);
            expense.setCreatedBy(rs.getInt("created_by"));

            return expense;
        }, groupId);
    }

    @Override
    @Transactional
    public Expense findById(int expenseId) {
        final String sql = "select "
                + "e.expense_id, "
                + "e.`name` as expense_name, "
                + "e.total_cost, "
                + "e.category, "
                + "e.`description` as expense_description, "
                + "e.created_at, "

                + "g.group_id, "
                + "g.`name` as group_name, "
                + "g.`description` as group_description, "

                + "gcb.user_id as gcb_user_id, "
                + "gcb.first_name as gcb_first_name, "
                + "gcb.last_name as gcb_last_name, "
                + "gcb.email as gcb_email, "
                + "gcb.username as gcb_username, "
                + "gcb.password_hash as gcb_password_hash, "
                + "gcb.disabled as gcb_disabled, "

                + "ecb.user_id as ecb_user_id, "
                + "ecb.first_name as ecb_first_name, "
                + "ecb.last_name as ecb_last_name, "
                + "ecb.email as ecb_email, "
                + "ecb.username as ecb_username, "
                + "ecb.password_hash as ecb_password_hash, "
                + "ecb.disabled as ecb_disabled "

                + "from expense e "
                + "inner join `group` as g on e.group_id = g.group_id "
                + "inner join `user` as gcb on g.created_by = gcb.user_id "
                + "inner join `user` as ecb on e.created_by = ecb.user_id "
                + "where e.expense_id = ?;";

        Expense expense = jdbcTemplate.query(sql,
                        (rs, rowNum) -> new ExpenseMapper(this)
                                .mapRow(rs, rowNum, "", "", "gcb_", "ecb_")
                        , expenseId)
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (expense != null) {
            //addReceipts(expense);
            //addComments(expense);
            addUsers(expense);
        }

        return expense;
    }

    @Override
    @Transactional
    public Expense add(Expense expense) {
        if (expense == null) {
            return null;
        }

        final String expenseSql = "insert into expense (`name`, total_cost, category, `description`, created_at, group_id, created_by) "
                + "values (?,?,?,?,?,?,?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(expenseSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, expense.getName());
            ps.setBigDecimal(2, expense.getTotalCost());
            ps.setString(3, expense.getCategory().toString());
            ps.setString(4, expense.getDescription());
            ps.setTimestamp(5, Timestamp.valueOf(expense.getCreatedAt()));
            ps.setInt(6, expense.getGroupId());
            ps.setInt(7, expense.getCreatedBy());
            return ps;
        }, keyHolder);

        if (rowsAffected <= 0) {
            return null;
        }

        expense.setExpenseId(keyHolder.getKey().intValue());

        if (expense.getUserExpenses() != null && !expense.getUserExpenses().isEmpty()) {
            addUserExpenses(expense.getExpenseId(), expense.getUserExpenses());
        }

        return expense;
    }

    private void addUserExpenses(int expenseId, List<UserExpense> userExpenses) {
        final String userExpenseSql = "insert into user_expense (user_id, expense_id, amount_owned, amount_paid) "
                + "values (?, ?, ?, ?);";

        for (UserExpense userExpense : userExpenses) {
            jdbcTemplate.update(userExpenseSql,
                    userExpense.getUserId(),
                    expenseId,
                    userExpense.getAmountOwed(),
                    userExpense.getAmountPaid()
            );
        }
    }

    @Override
    public boolean update(Expense expense) {
        if (expense == null) {
            return false;
        }

        final String sql = "update expense set "
                + "`name` = ?, "
                + "total_cost = ?, "
                + "category = ?, "
                + "`description` = ?, "
                + "created_at = ? "
                + "where expense_id = ?;";

        return jdbcTemplate.update(sql,
                expense.getName(),
                expense.getTotalCost(),
                expense.getCategory().toString(),
                expense.getDescription(),
                expense.getCreatedAt(),
                expense.getExpenseId()) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(int expenseId) {
        return jdbcTemplate.update("delete from expense where expense_id = ?;", expenseId) > 0;
    }

    private void addReceipts(Expense expense) {
        final String sql = "select receipt_id, expense_id, image_url, uploaded_at "
                + "from receipt "
                + "where expense_id = ? "
                + "order by uploaded_at desc;";

        var receipts = jdbcTemplate.query(sql, new ReceiptMapper(), expense.getExpenseId());
        expense.setReceipts(receipts);
    }

    private void addComments(Expense expense) {
        final String sql = "select c.comment_id, c.expense_id, c.content, c.timestamp, c.created_by, "
                + "u.first_name as first_name, u.last_name as last_name, u.email as email "
                + "from comment c "
                + "inner join user u on c.created_by = u.user_id "
                + "where c.expense_id = ? "
                + "order by c.timestamp asc;";

        var comments = jdbcTemplate.query(sql, new CommentMapper(), expense.getExpenseId());
        expense.setComments(comments);
    }

    @Override
    public List<String> getRolesByAppUserId(int userId) {
        final String sql = "select r.name "
                + "from user_role ur "
                + "inner join role r on ur.role_id = r.role_id "
                + "where ur.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"), userId);
    }

    private void addUsers(Expense expense) {
        final String sql = "select "
                + "ue.user_id as ue_user_id, "
                + "ue.expense_id as ue_expense_id, "
                + "ue.amount_owned as ue_amount_owned, "
                + "ue.amount_paid as ue_amount_paid, "

                + "u.user_id as u_user_id, "
                + "u.first_name as u_first_name, "
                + "u.last_name as u_last_name, "
                + "u.email as u_email, "
                + "u.username as u_username, "
                + "u.password_hash as u_password_hash, "
                + "u.disabled as u_disabled, "

                + "e.expense_id, "
                + "e.`name` as expense_name, "
                + "e.total_cost, "
                + "e.category, "
                + "e.`description` as expense_description, "
                + "e.created_at, "

                + "eg.group_id as eg_group_id, "
                + "eg.`name` as eg_group_name, "
                + "eg.`description` as eg_group_description, "

                + "egcb.user_id as egcb_user_id, "
                + "egcb.first_name as egcb_first_name, "
                + "egcb.last_name as egcb_last_name, "
                + "egcb.email as egcb_email, "
                + "egcb.username as egcb_username, "
                + "egcb.password_hash as egcb_password_hash, "
                + "egcb.disabled as egcb_disabled, "

                + "ecb.user_id as ecb_user_id, "
                + "ecb.first_name as ecb_first_name, "
                + "ecb.last_name as ecb_last_name, "
                + "ecb.email as ecb_email, "
                + "ecb.username as ecb_username, "
                + "ecb.password_hash as ecb_password_hash, "
                + "ecb.disabled as ecb_disabled "

                + "from user_expense as ue "
                + "inner join `user` as u on ue.user_id = u.user_id "
                + "inner join expense as e on ue.expense_id = e.expense_id "
                + "inner join `group` as eg on e.group_id = eg.group_id "
                + "inner join `user` as egcb on eg.created_by = egcb.user_id "
                + "inner join `user` as ecb on e.created_by = ecb.user_id "
                + "where ue.expense_id = ?;";

        UserExpenseMapper userExpenseMapper = new UserExpenseMapper(this);

        List<UserExpense> userExpenses = jdbcTemplate.query(sql,
                (rs, rowNum) -> userExpenseMapper.mapRow(rs, rowNum, "ue_", "u_", "", "eg_", "egcb_", "ecb_"),
                expense.getExpenseId()
        );

        expense.setUserExpenses(userExpenses);
    }

}
