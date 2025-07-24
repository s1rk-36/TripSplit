package learn.tripsplit.domain;

import learn.tripsplit.data.AppUserRepository;
import learn.tripsplit.data.UserExpenseRepository;
import learn.tripsplit.data.ExpenseRepository;
import learn.tripsplit.models.AppUser;
import learn.tripsplit.models.UserExpense;
import learn.tripsplit.models.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserExpenseService {

    @Autowired
    private UserExpenseRepository userExpenseRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    public List<UserExpense> findByUserId(int userId) {
        return userExpenseRepository.findByUserId(userId);
    }

    public List<UserExpense> findByExpenseId(int expenseId) {
        return userExpenseRepository.findByExpenseId(expenseId);
    }

    public UserExpense findByUserIdAndExpenseId(int userId, int expenseId) {
        return userExpenseRepository.findByUserIdAndExpenseId(userId, expenseId);
    }

    @Transactional
    public Result<UserExpense> add(UserExpense userExpense) {
        Result<UserExpense> result = new Result<>();
        // Validation
        if (userExpense == null) {
            result.addMessage("UserExpense cannot be null", ResultType.INVALID);
        }

        if (userExpense.getUserId() <= 0) {
            result.addMessage("Valid user ID is required", ResultType.INVALID);
        }

        if (userExpense.getExpenseId() <= 0) {
            result.addMessage("Valid expense ID is required", ResultType.INVALID);
        }

        if (userExpense.getAmountOwed() == null || userExpense.getAmountOwed().compareTo(BigDecimal.ZERO) < 0) {
            result.addMessage("Amount owed cannot be negative", ResultType.INVALID);
        }

        if (userExpense.getAmountPaid() == null || userExpense.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            result.addMessage("Amount paid cannot be negative", ResultType.INVALID);
        }

        // Verify user and expense exist
        AppUser user = appUserRepository.findById(userExpense.getUserId());
        if (user == null) {
            result.addMessage("User not found with ID: " + userExpense.getUserId(), ResultType.INVALID);
        }

        Expense expense = expenseRepository.findById(userExpense.getExpenseId());
        if (expense == null) {
            result.addMessage("Expense not found with ID: " + userExpense.getExpenseId(), ResultType.INVALID);
        }

        UserExpense ue = userExpenseRepository.add(userExpense);
        if (ue == null) {
            result.addMessage("Failed to add user expense", ResultType.INVALID);
        }

        result.setPayload(ue);
        return result;
    }

    @Transactional
    public Result<UserExpense> update(UserExpense userExpense) {
        Result<UserExpense> result = new Result<>();

        if (userExpense.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            result.addMessage("Amount paid cannot be negative", ResultType.INVALID);
            return result;
        }

        if (userExpense.getAmountOwed().compareTo(BigDecimal.ZERO) < 0) {
            result.addMessage("Amount owed cannot be negative", ResultType.INVALID);
            return result;
        }

        boolean updated = userExpenseRepository.update(userExpense);
        if (!updated) {
            result.addMessage("User expense not found or could not be updated", ResultType.NOT_FOUND);
            return result;
        }

        result.setPayload(userExpense);
        return result;
    }

    @Transactional
    public boolean deleteByUserIdAndExpenseId(int userId, int expenseId) {
        return userExpenseRepository.deleteByUserIdAndExpenseId(userId, expenseId);
    }

    public BigDecimal calculateUserBalance(int userId) {
        List<UserExpense> userExpenses = findByUserId(userId);

        BigDecimal totalOwed = userExpenses.stream()
                .map(UserExpense::getAmountOwed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = userExpenses.stream()
                .map(UserExpense::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPaid.subtract(totalOwed);
    }
}