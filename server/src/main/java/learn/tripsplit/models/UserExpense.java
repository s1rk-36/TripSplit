package learn.tripsplit.models;

import java.math.BigDecimal;
import java.util.Objects;

public class UserExpense {
    // Fields
    private int userId;
    private int expenseId;
    private BigDecimal amountOwed;
    private BigDecimal amountPaid;

    private AppUser appUser;
    private Expense expense;

    // Constructors
    public UserExpense() {}

    public UserExpense(int userId, int expenseId, BigDecimal amountOwed, BigDecimal amountPaid) {
        this.userId = userId;
        this.expenseId = expenseId;
        this.amountOwed = amountOwed;
        this.amountPaid = amountPaid;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public BigDecimal getAmountOwed() { return amountOwed; }
    public void setAmountOwed(BigDecimal amountOwed) { this.amountOwed = amountOwed; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public AppUser getUser() { return appUser; }
    public void setUser(AppUser appUser) { this.appUser = appUser; }

    public Expense getExpense() {
        return expense;
    }
    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserExpense that = (UserExpense) o;
        return userId == that.userId && expenseId == that.expenseId && Objects.equals(amountOwed, that.amountOwed) && Objects.equals(amountPaid, that.amountPaid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, expenseId, amountOwed, amountPaid);
    }

}