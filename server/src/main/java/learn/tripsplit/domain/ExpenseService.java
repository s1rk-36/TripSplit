package learn.tripsplit.domain;

import learn.tripsplit.data.ExpenseRepository;
import learn.tripsplit.models.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    public List<Expense> findByGroupId(int groupId) {
        return expenseRepository.findByGroupId(groupId);
    }

    public Expense findById(int expenseId) {
        return expenseRepository.findById(expenseId);
    }

    @Transactional
    public Result<Expense> add(Expense expense) {
        Result<Expense> result = validate(expense);

        if (!result.isSuccess()) {
            return result;
        }

        // Set creation time if not provided
        if (expense.getCreatedAt() == null) {
            expense.setCreatedAt(LocalDateTime.now());
        }

        expense = expenseRepository.add(expense);
        result.setPayload(expense);
        return result;
    }

    @Transactional
    public Result<Expense> update(Expense expense) {
        Result<Expense> result = validate(expense);

        if (!result.isSuccess()) {
            return result;
        }

        if (!expenseRepository.update(expense)) {
            String msg = String.format("expenseId: %s, not found", expense.getExpenseId());
            result.addMessage(msg, ResultType.NOT_FOUND);
        }

        return result;
    }

    @Transactional
    public Result<Void> deleteById(int expenseId) {
        if (expenseId <= 0) {
            Result<Void> result = new Result<>();
            result.addMessage("Expense ID must be greater than zero.", ResultType.INVALID);
            return result;
        }

        if (!expenseRepository.deleteById(expenseId)) {
            Result<Void> result = new Result<>();
            result.addMessage("Expense not found or delete failed.", ResultType.NOT_FOUND);
            return result;
        }

        return new Result<>(); // Success
    }

    private Result<Expense> validate(Expense expense) {
        Result<Expense> result = new Result<>();
        if (expense == null) {
            result.addMessage("expense cannot be null", ResultType.INVALID);
            return result;
        }

        if (expense.getName() == null || expense.getName().isBlank()) {
            result.addMessage("name is required", ResultType.INVALID);
        }

        if (expense.getName() != null && expense.getName().length() > 50) {
            result.addMessage("name must be 50 characters or less", ResultType.INVALID);
        }

        if(expense.getCategory() == null){
            result.addMessage("category can not be null", ResultType.INVALID);
        }

        if(expense.getTotalCost().compareTo(BigDecimal.ZERO) <= 0){
            result.addMessage("totalCost can be less than or equal to 0", ResultType.INVALID);
        }

        if(expense.getCreatedBy() == null || expense.getCreatedBy().getAppUserId() <= 0){
            result.addMessage("valid creator required", ResultType.INVALID);
        }

        return result;
    }
}