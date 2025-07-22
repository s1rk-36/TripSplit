package learn.tripsplit.data;

import learn.tripsplit.data.mappers.CommentMapper;
import learn.tripsplit.models.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class CommentJdbcTemplateRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommentJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Comment> findAll() {
        final String sql = "select comment_id, expense_id, content, timestamp, created_by "
                + "from comments limit 1000;";
        return jdbcTemplate.query(sql, new CommentMapper());
    }

    @Override
    public List<Comment> findByExpenseId(int expenseId) {
        final String sql = "select c.comment_id, c.expense_id, c.content, c.timestamp, c.created_by, "
                + "u.first_name as first_name, u.last_name as last_name, u.email as email "
                + "from comments c "
                + "inner join users u on c.created_by = u.user_id "
                + "where c.expense_id = ? "
                + "order by c.timestamp asc;";
        return jdbcTemplate.query(sql, new CommentMapper(), expenseId);
    }

    @Override
    public Comment findById(int commentId) {
        final String sql = "select c.comment_id, c.expense_id, c.content, c.timestamp, c.created_by, "
                + "u.first_name as first_name, u.last_name as last_name, u.email as email "
                + "from comments c "
                + "inner join users u on c.created_by = u.user_id "
                + "where c.comment_id = ?;";

        return jdbcTemplate.query(sql, new CommentMapper(), commentId).stream()
                .findFirst().orElse(null);
    }

    @Override
    public Comment add(Comment comment) {
        final String sql = "insert into comments (expense_id, content, timestamp, created_by) "
                + "values (?,?,?,?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, comment.getExpenseId());
            ps.setString(2, comment.getContent());
            ps.setTimestamp(3, Timestamp.valueOf(comment.getTimestamp()));
            ps.setInt(4, comment.getCreatedBy().getUserId());
            return ps;
        }, keyHolder);

        if (rowsAffected <= 0) {
            return null;
        }

        comment.setCommentId(keyHolder.getKey().intValue());
        return comment;
    }

    @Override
    public boolean update(Comment comment) {
        final String sql = "update comments set "
                + "content = ? "
                + "where comment_id = ?;";

        return jdbcTemplate.update(sql,
                comment.getContent(),
                comment.getCommentId()) > 0;
    }

    @Override
    public boolean deleteById(int commentId) {
        return jdbcTemplate.update("delete from comments where comment_id = ?;", commentId) > 0;
    }
}