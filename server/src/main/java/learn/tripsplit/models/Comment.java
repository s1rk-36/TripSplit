package learn.tripsplit.models;

import java.time.LocalDateTime;

public class Comment {
    private int commentId;
    private int expenseId;
    private String content;
    private LocalDateTime timestamp;
    private User createdBy;

    public Comment() {}

    public Comment(int expenseId, String content, User createdBy) {
        this.expenseId = expenseId;
        this.content = content;
        this.createdBy = createdBy;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}