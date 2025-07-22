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
    private String category;
    private String description;
    private LocalDateTime createdAt;
    private Group group;
    private AppUser createdBy;
    private List<Receipt> receipts;
    private List<Comment> comments;
    private List<UserExpense> users = new ArrayList<>();

    // Constructors
    public Expense() {}

    public Expense(int expenseId, String name, BigDecimal totalCost, String category, String description, LocalDateTime createdAt, Group group, AppUser createdBy) {
        this.expenseId = expenseId;
        this.name = name;
        this.totalCost = totalCost;
        this.category = category;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.group = group;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUser createdBy) {
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

    public List<UserExpense> getUsers() {
        return users;
    }

    public void setUsers(List<UserExpense> users) {
        this.users = users;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return expenseId == expense.expenseId && Objects.equals(name, expense.name) && Objects.equals(totalCost, expense.totalCost) && Objects.equals(category, expense.category) && Objects.equals(description, expense.description) && Objects.equals(createdAt, expense.createdAt) && Objects.equals(group, expense.group) && Objects.equals(createdBy, expense.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, name, totalCost, category, description, createdAt, group, createdBy);
    }

}
