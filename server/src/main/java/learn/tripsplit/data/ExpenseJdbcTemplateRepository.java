package learn.tripsplit.data;

import learn.tripsplit.data.mappers.ExpenseMapper;
import learn.tripsplit.data.mappers.ReceiptMapper;
import learn.tripsplit.data.mappers.CommentMapper;
import learn.tripsplit.data.mappers.UserExpenseMapper;
import learn.tripsplit.models.Expense;
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
public class ExpenseJdbcTemplateRepository implements ExpenseRepository {

    private final JdbcTemplate jdbcTemplate;

    public ExpenseJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Expense> findAll() {
        final String sql = "select e.expense_id, e.group_id, e.name, e.total_cost, e.category, e.description, e.created_at, e.created_by, "
                + "u.first_name as creator_first_name, u.last_name as creator_last_name, u.email as creator_email "
                + "from expense e "
                + "inner join `user` u on e.created_by = u.user_id "
                + "order by e.created_at desc "
                + "limit 1000;";
        return jdbcTemplate.query(sql, new ExpenseMapper());
    }

    @Override
    public List<Expense> findByGroupId(int groupId) {
        final String sql = "select e.expense_id, e.group_id, e.name, e.total_cost, e.category, e.description, e.created_at, e.created_by, "
                + "u.first_name as first_name, u.last_name as last_name, u.email as email "
                + "from expense e "
                + "inner join `user` u on e.created_by = u.user_id "
                + "where e.group_id = ? "
                + "order by e.created_at desc;";

        List<Expense> expenses = jdbcTemplate.query(sql, new ExpenseMapper(), groupId);

        for (Expense expense : expenses) {
            addReceipts(expense);
            addComments(expense);
        }

        return expenses;
    }

    @Override
    @Transactional
    public Expense findById(int expenseId) {
        final String sql = "select e.expense_id, e.group_id, e.name, e.total_cost, e.category, e.description, e.created_at, e.created_by, "
                + "u.first_name as first_name, u.last_name as last_name, u.email as email "
                + "from expense e "
                + "inner join `user` u on e.created_by = u.user_id "
                + "where e.expense_id = ?;";

        Expense expense = jdbcTemplate.query(sql, new ExpenseMapper(), expenseId).stream()
                .findFirst().orElse(null);

        if (expense != null) {
            addReceipts(expense);
            addComments(expense);
        }

        return expense;
    }

    @Override
    public Expense add(Expense expense) {
        final String sql = "insert into expense (group_id, name, total_cost, category, description, created_by, created_at) "
                + "values (?,?,?,?,?,?,?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, expense.getGroupId());
            ps.setString(2, expense.getName());
            ps.setBigDecimal(3, expense.getTotalCost());
            ps.setString(4, expense.getCategory());
            ps.setString(5, expense.getDescription());
            ps.setInt(6, expense.getCreatedBy().getUserId());
            ps.setTimestamp(7, Timestamp.valueOf(expense.getCreatedAt()));
            return ps;
        }, keyHolder);

        if (rowsAffected <= 0) {
            return null;
        }

        expense.setExpenseId(keyHolder.getKey().intValue());
        return expense;
    }

    @Override
    public boolean update(Expense expense) {
        final String sql = "update expense set "
                + "name = ?, "
                + "total_cost = ?, "
                + "category = ?, "
                + "description = ? "
                + "where expense_id = ?;";

        return jdbcTemplate.update(sql,
                expense.getName(),
                expense.getTotalCost(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getExpenseId()) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(int expenseId) {
        // Delete related records first
        jdbcTemplate.update("delete from receipt where expense_id = ?;", expenseId);
        jdbcTemplate.update("delete from comment where expense_id = ?;", expenseId);
        jdbcTemplate.update("delete from user_expense where expense_id = ?;", expenseId);

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

    private void addUsers(Expense expense) {
        final String sql = "select ue.id, ue.user_id, ue.expense_id, ue.amount_owed, ue.amount_paid, "
                + "u.first_name as user_first_name, u.last_name as user_last_name, u.email as user_email "
                + "from user_expense ue "
                + "inner join user u on ue.user_id = u.user_id "
                + "where ue.expense_id = ?;";

        var userExpenses = jdbcTemplate.query(sql, new UserExpenseMapper(), expense.getExpenseId());
        expense.setUsers(userExpenses);
    }
}
