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
        // Validation
        if (comment == null) {
            return makeResult("Comment cannot be null.");
        }

        if (comment.getExpenseId() <= 0) {
            return makeResult("Valid expense ID is required.");
        }

        if (comment.getContent() == null || comment.getContent().isBlank()) {
            return makeResult("Comment content is required.");
        }

        if (comment.getContent().length() > 1000) {
            return makeResult("Comment content cannot exceed 1000 characters.");
        }

        if (comment.getCreatedBy() == null || comment.getCreatedBy().getAppUserId() <= 0) {
            return makeResult("Valid creator is required.");
        }

        // Verify expense exists
        Expense expense = expenseRepository.findById(comment.getExpenseId());
        if (expense == null) {
            return makeResult("Expense not found with ID: " + comment.getExpenseId());
        }

        // Set timestamp if not provided
        if (comment.getTimestamp() == null) {
            comment.setTimestamp(LocalDateTime.now());
        }

        Comment result = commentRepository.add(comment);
        if (result == null) {
            return makeResult("Failed to add comment.");
        }

        return makeResult(null, result);
    }

    @Transactional
    public Result<Comment> update(Comment comment) {
        // Validation
        if (comment.getCommentId() <= 0) {
            return makeResult("Comment ID must be set for update.");
        }

        if (comment.getContent() == null || comment.getContent().isBlank()) {
            return makeResult("Comment content is required.");
        }

        if (comment.getContent().length() > 1000) {
            return makeResult("Comment content cannot exceed 1000 characters.");
        }

        if (!commentRepository.update(comment)) {
            return makeResult("Comment not found or update failed.");
        }

        return makeResult(null, comment);
    }

    @Transactional
    public Result<Void> deleteById(int commentId) {
        if (commentId <= 0) {
            Result<Void> result = new Result<>();
            result.addMessage("Comment ID must be greater than zero.", ResultType.INVALID);
            return result;
        }

        if (!commentRepository.deleteById(commentId)) {
            Result<Void> result = new Result<>();
            result.addMessage("Comment not found or delete failed.", ResultType.NOT_FOUND);
            return result;
        }

        return new Result<>(); // Success
    }

    // Helper methods
    private Result<Comment> makeResult(String message) {
        Result<Comment> result = new Result<>();
        result.addMessage(message, ResultType.INVALID);
        return result;
    }

    private Result<Comment> makeResult(String message, Comment comment) {
        Result<Comment> result = new Result<>();
        if (message != null) {
            result.addMessage(message, ResultType.INVALID);
        } else {
            result.setPayload(comment);
        }
        return result;
    }
}
