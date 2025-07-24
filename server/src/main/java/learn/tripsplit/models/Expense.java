package learn.tripsplit.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Expense {
    // Fields
    private int expenseId;
    private String name;
    private BigDecimal totalCost;
    private Category category;
    private String description;
    private LocalDateTime createdAt;
    private int groupId;
    private int createdBy;
    private List<Receipt> receipts;
    private List<Comment> comments;
    private List<UserExpense> userExpenses = new ArrayList<>();

    // Constructors
    public Expense() {}

    public Expense(int expenseId, String name, BigDecimal totalCost, Category category, String description, LocalDateTime createdAt, int groupId, int createdBy) {
        this.expenseId = expenseId;
        this.name = name;
        this.totalCost = totalCost;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
        this.groupId = groupId;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<UserExpense> getUserExpenses() { return userExpenses; }

    public void setUserExpenses(List<UserExpense> userExpenses) { this.userExpenses = userExpenses; }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return expenseId == expense.expenseId && Objects.equals(name, expense.name) && Objects.equals(totalCost, expense.totalCost) && Objects.equals(category, expense.category) && Objects.equals(description, expense.description) && Objects.equals(createdAt, expense.createdAt) && Objects.equals(groupId, expense.groupId) && Objects.equals(createdBy, expense.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, name, totalCost, category, description, createdAt, groupId, createdBy);
    }

}
