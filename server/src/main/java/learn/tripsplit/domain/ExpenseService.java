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

        if (expense.getExpenseId() != 0) {
            result.addMessage("expenseId should not be set for `add` operation", ResultType.INVALID);
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

        if (expense.getExpenseId() <= 0) {
            result.addMessage("expenseId must be set for `update` operation", ResultType.INVALID);
            return result;
        }

        if (!expenseRepository.update(expense)) {
            String msg = String.format("expenseId: %s, not found", expense.getExpenseId());
            result.addMessage(msg, ResultType.NOT_FOUND);
        }

        return result;
    }

    @Transactional
    public boolean deleteById(int expenseId) {
        return expenseRepository.deleteById(expenseId);
    }

    private Result<Expense> validate(Expense expense) {
        Result<Expense> result = new Result<>();

        if (expense == null) {
            result.addMessage("expense cannot be null", ResultType.INVALID);
            return result;
        }

        if (expense.getName() == null || expense.getName().isBlank()) {
            result.addMessage("expense name is required", ResultType.INVALID);
        } else if (expense.getName().length() < 3 || expense.getName().length() > 100) {
            result.addMessage("expense name must be between 3 and 100 characters", ResultType.INVALID);
        }

        if (expense.getTotalCost() == null || expense.getTotalCost().compareTo(BigDecimal.ZERO) <= 0) {
            result.addMessage("total cost is required and must be greater than 0", ResultType.INVALID);
        }

        if (expense.getCategory() == null){
            result.addMessage("category cannot be null", ResultType.INVALID);
        }

        if (expense.getCreatedAt() == null || !expense.getCreatedAt().isBefore(LocalDateTime.now())) {
            result.addMessage("expense createdAt is required and must be in the past", ResultType.INVALID);
        }

        if (expense.getGroup() == null || expense.getGroup().getGroupId() <= 0) {
            result.addMessage("valid group is required", ResultType.INVALID);
        }

        if (expense.getCreatedBy() == null || expense.getCreatedBy().getAppUserId() <= 0){
            result.addMessage("valid expense creator required", ResultType.INVALID);
        }

        return result;
    }
}