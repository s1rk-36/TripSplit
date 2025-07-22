package learn.tripsplit.data.mappers;

import learn.tripsplit.models.Comment;
import learn.tripsplit.models.User;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CommentMapper implements RowMapper<Comment> {

    @Override
    public Comment mapRow(ResultSet resultSet, int i) throws SQLException {
        Comment comment = new Comment();
        comment.setCommentId(resultSet.getInt("comment_id"));
        comment.setExpenseId(resultSet.getInt("expense_id"));
        comment.setContent(resultSet.getString("content"));
        comment.setTimestamp(resultSet.getTimestamp("timestamp").toLocalDateTime());

        // Map created_by user if available
        if (resultSet.getMetaData().getColumnCount() > 4) {
            User createdBy = new User();
            createdBy.setUserId(resultSet.getInt("created_by"));
            if (resultSet.getString("first_name") != null) {
                createdBy.setFirstName(resultSet.getString("first_name"));
                createdBy.setLastName(resultSet.getString("last_name"));
                createdBy.setEmail(resultSet.getString("email"));
            }
            comment.setCreatedBy(createdBy);
        }

        return comment;
    }
}