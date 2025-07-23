package learn.tripsplit.data.mappers;

import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.Comment;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CommentMapper implements RowMapper<Comment> {

    @Override
    public Comment mapRow(ResultSet resultSet, int i) throws SQLException {
        Comment comment = new Comment();
        comment.setCommentId(resultSet.getInt("comment_id"));
        comment.setExpenseId(resultSet.getInt("expense_id"));
        comment.setContent(resultSet.getString("content"));
        comment.setTimestamp(resultSet.getTimestamp("timestamp").toLocalDateTime());

//        // Map created_by user if available
//        if (resultSet.getMetaData().getColumnCount() > 4) {
//            AppUser createdBy = new AppUser();
//            createdBy.setAppUserId(resultSet.getInt("created_by"));
//            if (resultSet.getString("first_name") != null) {
//                createdBy.setFirstName(resultSet.getString("first_name"));
//                createdBy.setLastName(resultSet.getString("last_name"));
//                createdBy.setEmail(resultSet.getString("email"));
//            }
//            comment.setCreatedBy(createdBy);
//        }
//
//        return comment;

        // Check if user columns are included in the result
        if (resultSet.getMetaData().getColumnCount() > 4
                && resultSet.getString("first_name") != null) {

            int appUserId = resultSet.getInt("created_by");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String email = resultSet.getString("email");

            // Placeholder values for required fields
            String username = resultSet.getString("username") != null ? resultSet.getString("username") : "N/A";
            String passwordHash = "N/A"; // not loaded here, but required by constructor
            boolean disabled = false;
            List<String> roles = List.of("USER");

            AppUser createdBy = new AppUser(
                    appUserId,
                    firstName,
                    lastName,
                    email,
                    username,
                    passwordHash,
                    disabled,
                    roles
            );
            comment.setCreatedBy(createdBy);
        }

        return comment;
    }
}