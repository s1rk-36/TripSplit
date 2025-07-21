package learn.tripsplit.models;

import java.math.BigDecimal;

public class UserExpense {
    private int id;
    private int userId;
    private int expenseId;
    private BigDecimal amountOwed;
    private BigDecimal amountPaid;
    private User user;

    public UserExpense() {}

    public UserExpense(int userId, int expenseId, BigDecimal amountOwed, BigDecimal amountPaid) {
        this.userId = userId;
        this.expenseId = expenseId;
        this.amountOwed = amountOwed;
        this.amountPaid = amountPaid;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public BigDecimal getAmountOwed() { return amountOwed; }
    public void setAmountOwed(BigDecimal amountOwed) { this.amountOwed = amountOwed; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}