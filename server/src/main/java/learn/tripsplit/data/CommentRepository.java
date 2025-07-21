package learn.tripsplit.data;

import learn.tripsplit.models.Comment;
import java.util.List;

public interface CommentRepository {
    List<Comment> findAll();
    List<Comment> findByExpenseId(int expenseId);
    Comment findById(int commentId);
    Comment add(Comment comment);
    boolean update(Comment comment);
    boolean deleteById(int commentId);
}