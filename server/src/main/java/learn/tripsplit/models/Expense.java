package learn.tripsplit.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Expense {
    private int expenseId;
    private int groupId;
    private String name;
    private BigDecimal totalCost;
    private String category;
    private String description;
    private User createdBy;
    private LocalDateTime createdAt;
    private List<Receipt> receipts;
    private List<Comment> comments;
    private List<UserExpense> userExpenses;

    public Expense() {}

    public Expense(int groupId, String name, BigDecimal totalCost, String category, String description, User createdBy) {
        this.groupId = groupId;
        this.name = name;
        this.totalCost = totalCost;
        this.category = category;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Receipt> getReceipts() { return receipts; }
    public void setReceipts(List<Receipt> receipts) { this.receipts = receipts; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public List<UserExpense> getUserExpenses() { return userExpenses; }
    public void setUserExpenses(List<UserExpense> userExpenses) { this.userExpenses = userExpenses; }
}
