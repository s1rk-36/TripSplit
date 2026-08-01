package learn.tripsplit.domain;

import learn.tripsplit.data.ExpenseRepository;
import learn.tripsplit.data.UserExpenseRepository;
import learn.tripsplit.models.Expense;
import learn.tripsplit.models.UserExpense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserExpenseRepository userExpenseRepository;

    /** Expenses across every group the user belongs to. */
    public List<Expense> findByMemberUserId(int userId) {
        return expenseRepository.findByMemberUserId(userId);
    }

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

    public List<Expense> findByGroupIdWithUserExpenses(int groupId) {
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        // Two queries total rather than one per expense. Against a hosted database
        // each round trip costs a few hundred milliseconds, so the old loop made a
        // group's ledger take seconds to load.
        Map<Integer, List<UserExpense>> splitsByExpense = new HashMap<>();
        for (UserExpense split : userExpenseRepository.findByGroupId(groupId)) {
            splitsByExpense.computeIfAbsent(split.getExpenseId(), k -> new ArrayList<>()).add(split);
        }

        for (Expense expense : expenses) {
            expense.setUserExpenses(
                    splitsByExpense.getOrDefault(expense.getExpenseId(), new ArrayList<>()));
        }
        return expenses;
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

        if (expense.getGroupId() <= 0) {
            result.addMessage("valid group is required", ResultType.INVALID);
        }

        if (expense.getCreatedBy() <= 0){
            result.addMessage("valid expense creator required", ResultType.INVALID);
        }

        return result;
    }
}