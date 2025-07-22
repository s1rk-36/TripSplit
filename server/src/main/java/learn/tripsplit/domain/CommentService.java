package learn.tripsplit.domain;

import learn.tripsplit.data.CommentRepository;
import learn.tripsplit.data.ExpenseRepository;
import learn.tripsplit.models.Comment;
import learn.tripsplit.models.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public List<Comment> findByExpenseId(int expenseId) {
        return commentRepository.findByExpenseId(expenseId);
    }

    public Comment findById(int commentId) {
        return commentRepository.findById(commentId);
    }

    @Transactional
    public Result<Comment> add(Comment comment) {
        Result<Comment> result = new Result<>();
        // Validation
        if (comment == null) {
            result.addMessage("Comment cannot be null.", ResultType.INVALID);
        }

        if (comment.getExpenseId() <= 0) {
            result.addMessage("Valid expense ID is required.", ResultType.INVALID);
        }

        if (comment.getContent() == null || comment.getContent().isBlank()) {
            result.addMessage("Comment content is required.", ResultType.INVALID);
        }

        if (comment.getContent().length() > 1000) {
            result.addMessage("Comment content cannot exceed 1000 characters.", ResultType.INVALID);
        }

        if (comment.getCreatedBy() == null || comment.getCreatedBy().getAppUserId() <= 0) {
            result.addMessage("Valid creator is required.", ResultType.INVALID);
        }

        // Verify expense exists
        Expense expense = expenseRepository.findById(comment.getExpenseId());
        if (expense == null) {
            result.addMessage("Expense not found with ID: " + comment.getExpenseId(), ResultType.NOT_FOUND);
        }

        // Set timestamp if not provided
        if (comment.getTimestamp() == null) {
            comment.setTimestamp(LocalDateTime.now());
        }

        Comment comment1 = commentRepository.add(comment);
        if (comment1 == null) {
            result.addMessage("Failed to add comment.", ResultType.INVALID);
        }

        result.setPayload(comment1);
        return result;
    }

    @Transactional
    public Result<Comment> update(Comment comment) {
        Result<Comment> result = new Result<>();

        // Validation
        if (comment.getCommentId() <= 0) {
            result.addMessage("Comment ID must be set for update.", ResultType.INVALID);
        }

        if (comment.getContent() == null || comment.getContent().isBlank()) {
            result.addMessage("Comment content is required.", ResultType.INVALID);
        }

        if (comment.getContent().length() > 1000) {
            result.addMessage("Comment content cannot exceed 1000 characters.", ResultType.INVALID);
        }

        if (!commentRepository.update(comment)) {
            result.addMessage("Comment not found or update failed.", ResultType.NOT_FOUND);
        }

        return result;
    }

    @Transactional
    public boolean deleteById(int commentId) {
        return commentRepository.deleteById(commentId);
    }

}
